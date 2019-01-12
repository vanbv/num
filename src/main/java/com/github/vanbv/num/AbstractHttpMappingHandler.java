package com.github.vanbv.num;

import com.github.vanbv.num.annotation.*;
import com.github.vanbv.num.exception.NumException;
import com.github.vanbv.num.exception.ParamException;
import com.github.vanbv.num.json.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHttpMappingHandler extends ChannelInboundHandlerAdapter {

    private static final String URL_PARAM_REGEX = "\\{(\\w*?)\\}";
    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<>();

    static {
        PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
        PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
        PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
        PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
        PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
        PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
    }

    private final List<HttpMethodHandler> httpMethodHandlers;
    private JsonParser parser;

    public AbstractHttpMappingHandler() {
        httpMethodHandlers = initHandler();
        this.parser = null;
    }

    public AbstractHttpMappingHandler(JsonParser parser) {
        this.parser = parser;
        httpMethodHandlers = initHandler();
    }

    private List<HttpMethodHandler>  initHandler() {
        List<HttpMethodHandler> handlers = new ArrayList<>();

        for (Method method : getClass().getMethods()) {
            Annotation annotation = method.getAnnotation(Get.class);
            HttpMethod httpMethod = HttpMethod.GET;

            if (annotation == null) {
                annotation = method.getAnnotation(Post.class);
                httpMethod = HttpMethod.POST;
            } else if (method.getAnnotation(Post.class) != null) {
                throw new NumException(String.format("Unknown method type, method - '%s'", method.getName()));
            }

            if (annotation == null) {
                annotation = method.getAnnotation(Put.class);
                httpMethod = HttpMethod.PUT;
            } else if (method.getAnnotation(Put.class) != null) {
                throw new NumException(String.format("Unknown method type, method - '%s'", method.getName()));
            }

            if (annotation == null) {
                annotation = method.getAnnotation(Delete.class);
                httpMethod = HttpMethod.DELETE;
            } else if (method.getAnnotation(Delete.class) != null) {
                throw new NumException(String.format("Unknown method type, method - '%s'", method.getName()));
            }

            if (annotation != null) {
                try {
                    Method value = annotation.getClass().getMethod("value");
                    String path = (String) value.invoke(annotation);

                    if (path != null) {
                        List<MethodParam> params = new ArrayList<>();
                        Matcher m = Pattern.compile(URL_PARAM_REGEX).matcher(path);
                        List<String> pathParamNames = new ArrayList<>();

                        while (m.find()) {
                            pathParamNames.add(m.group(1));
                        }

                        List<String> methodPathParamNames = new ArrayList<>();
                        boolean isExistsRequestBody = false;

                        for (Parameter parameter : method.getParameters()) {
                            QueryParam queryParamAnnotation = parameter.getAnnotation(QueryParam.class);
                            PathParam pathParamAnnotation = parameter.getAnnotation(PathParam.class);
                            RequestBody requestBodyAnnotation = parameter.getAnnotation(RequestBody.class);

                            if ((queryParamAnnotation != null && pathParamAnnotation != null)
                                    || (queryParamAnnotation != null && requestBodyAnnotation != null)
                                    || (pathParamAnnotation != null && requestBodyAnnotation != null)) {
                                throw new ParamException(String.format(
                                        "Multiple annotations for parameter '%s', path - '%s'",
                                        parameter.getName(), path));
                            }

                            if (queryParamAnnotation == null && pathParamAnnotation == null
                                    && requestBodyAnnotation == null) {
                                throw new ParamException(String.format("No annotation for parameter '%s', path - '%s'",
                                        parameter.getName(), path));
                            }

                            if (requestBodyAnnotation != null) {
                                if (parser == null) {
                                    throw new ParamException(String.format(
                                            "Request body parameters cannot be used, it is necessary to initialize " +
                                                    "the parser, param - '%s', path - '%s'", parameter.getName(),
                                            path));
                                }

                                if (!HttpMethod.POST.equals(httpMethod) && !HttpMethod.PUT.equals(httpMethod)) {
                                    throw new ParamException(String.format(
                                            "Request body parameter is available only for post or put requests, " +
                                                    "param - '%s', path - '%s'", parameter.getName(), path));
                                }

                                if (isExistsRequestBody) {
                                    throw new ParamException(String.format(
                                            "There can be only one request body parameter, path - '%s'", path));
                                } else {
                                    isExistsRequestBody = true;
                                }
                            }

                            String name = null;
                            boolean isRequired;
                            MethodParam.TypeParam typeParam;

                            if (queryParamAnnotation != null) {
                                name = queryParamAnnotation.value();
                                isRequired = queryParamAnnotation.required();
                                typeParam = MethodParam.TypeParam.QUERY_PARAM;
                            } else if (pathParamAnnotation != null) {
                                name = pathParamAnnotation.value();
                                isRequired = true;
                                typeParam = MethodParam.TypeParam.PATH_PARAM;

                                if (!pathParamNames.contains(name)) {
                                    throw new ParamException(String.format(
                                            "Parameter '%s' not specified in the path - '%s'", parameter.getName(),
                                            path));
                                }

                                methodPathParamNames.add(name);
                            } else {
                                isRequired = false;
                                typeParam = MethodParam.TypeParam.REQUEST_BODY;
                            }

                            Class<?> type = PRIMITIVES_TO_WRAPPERS.get(parameter.getType()) != null ?
                                    PRIMITIVES_TO_WRAPPERS.get(parameter.getType()) : parameter.getType();
                            params.add(new MethodParam(name, type, isRequired, typeParam));
                        }

                        for (String paramNames : pathParamNames) {
                            if (!methodPathParamNames.contains(paramNames)) {
                                throw new ParamException(String.format(
                                        "Path parameter '%s' is not specified, path - '%s'", paramNames, path));
                            }
                        }

                        handlers.add(new HttpMethodHandler(httpMethod, path, method,
                                this, !params.isEmpty() ? params : null, pathParamNames));
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new NumException(e);
                }
            }
        }

        return handlers.isEmpty() ? null : handlers;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (httpMethodHandlers != null && msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            for (HttpMethodHandler handler : httpMethodHandlers) {
                Matcher matcher = handler.getPathPattern().matcher(request.uri());

                if (handler.getHttpMethod().equals(request.method()) && matcher.matches()) {
                    Map<String, String> pathParamValues = new HashMap<>();

                    for (int i = 0; i < matcher.groupCount(); i++) {
                        String value = matcher.group(i + 1);

                        if (value != null) {
                            pathParamValues.put(handler.getPathParamNames().get(i), value);
                        }
                    }

                    Object[] parameters = null;

                    if (handler.getParams() != null && !handler.getParams().isEmpty()) {
                        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
                        parameters = new Object[handler.getParams().size()];

                        for (int i = 0; i < handler.getParams().size(); i++ ) {
                            if (handler.getParams().get(i).getTypeParam().equals(MethodParam.TypeParam.REQUEST_BODY)) {
                                parameters[i] = parser.parse(handler.getPath(),
                                        ((HttpContent) request).content(), handler.getParams().get(i).getType());
                                continue;
                            }

                            String value;

                            if (handler.getParams().get(i).getTypeParam().equals(MethodParam.TypeParam.PATH_PARAM)) {
                                value = pathParamValues.get(handler.getParams().get(i).getName());
                            } else {
                                List<String> values =
                                        queryStringDecoder.parameters().get(handler.getParams().get(i).getName());
                                value = values != null && !values.isEmpty() ? values.get(0) : null;
                            }
                            if (value != null && !value.isEmpty()) {
                                try {
                                    if (handler.getParams().get(i).getType().equals(String.class)) {
                                        parameters[i] = value;
                                    } else {
                                        Method valueOfMethod = handler.getParams().get(i).getType().getMethod("valueOf",
                                                String.class);
                                        parameters[i] = valueOfMethod.invoke(null, value);
                                    }
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    throw new ParamException(String.format(
                                            "Error cast parameter '%s' with value '%s' to '%s, path - '%s'",
                                            handler.getParams().get(i).getName(), value,
                                            handler.getParams().get(i).getType(),
                                            handler.getPath()));
                                }
                            } else if (handler.getParams().get(i).isRequired()) {
                                throw new ParamException(String.format("No required parameter '%s', path - '%s'",
                                        handler.getParams().get(i).getName(), handler.getPath()));
                            }
                        }
                    }

                    FullHttpResponse response = handler.invoke(parameters);
                    ctx.writeAndFlush(response);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
    }
}
