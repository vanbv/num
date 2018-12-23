package com.github.vanbv.num;

import com.github.vanbv.num.exception.NumException;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class HttpMethodHandler {

    private static final String URL_FORMAT_REGEX = "(?:\\.\\{format\\})$";
    private static final String URL_FORMAT_MATCH_REGEX = "(?:\\\\.\\([\\\\w%]+?\\))?";
    private static final String URL_QUERY_STRING_REGEX = "(?:\\?.*?)?$";
    private static final String URL_PARAM_REGEX = "\\{(\\w*?)\\}";
    private static final String URL_PARAM_MATCH_REGEX =
            "\\([%\\\\w-.\\\\~!\\$&'\\\\(\\\\)\\\\*\\\\+,;=:\\\\[\\\\]@]+?\\)";

    private final HttpMethod httpMethod;
    private final String path;
    private Pattern pathPattern;
    private final Method method;
    private final AbstractHttpMappingHandler handler;
    private List<MethodParam> params;
    private final List<String> pathParamNames;

    public HttpMethodHandler(HttpMethod httpMethod, String path, Method method, AbstractHttpMappingHandler handler,
                             List<MethodParam> params, List<String> pathParamNames) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.pathPattern = Pattern.compile(path.replaceFirst(URL_FORMAT_REGEX, URL_FORMAT_MATCH_REGEX)
                .replaceAll(URL_PARAM_REGEX, URL_PARAM_MATCH_REGEX) + URL_QUERY_STRING_REGEX);
        this.pathParamNames = pathParamNames;
        this.method = method;
        this.handler = handler;
        this.params = params;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public List<MethodParam> getParams() {
        return params;
    }

    public FullHttpResponse invoke(Object[] parameters) {
        try {
            return (FullHttpResponse) method.invoke(handler, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NumException(e);
        }
    }

    public Pattern getPathPattern() {
        return pathPattern;
    }

    public List<String> getPathParamNames() {
        return pathParamNames;
    }
}
