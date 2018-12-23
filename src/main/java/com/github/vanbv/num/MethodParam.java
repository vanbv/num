package com.github.vanbv.num;

public class MethodParam {

    public enum TypeParam {
        PATH_PARAM, QUERY_PARAM, REQUEST_BODY
    }

    private final String name;
    private final Class<?> type;
    private final boolean required;
    private final TypeParam typeParam;

    public MethodParam(String name, Class<?> type, boolean required, TypeParam typeParam) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.typeParam = typeParam;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public TypeParam getTypeParam() {
        return typeParam;
    }
}
