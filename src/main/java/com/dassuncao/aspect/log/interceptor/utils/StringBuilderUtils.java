package com.dassuncao.aspect.log.interceptor.utils;

import java.lang.reflect.Method;

public class StringBuilderUtils {

    public static StringBuilder appendClass(
            final StringBuilder builder,
            final Method method
    ) {
        return builder
                .append("class=")
                .append(method.getDeclaringClass().getSimpleName())
                .append(", ");
    }

    public static StringBuilder appendMethod(
            final StringBuilder builder,
            final Method method
    ) {
        return builder
                .append("method=")
                .append(method.getName())
                .append(", ");
    }

    public static StringBuilder appendParameter(
            final StringBuilder builder,
            final String parameterName,
            final Object parameterValue
    ) {
        return builder
                .append(parameterName)
                .append("=")
                .append(parameterValue)
                .append(", ");
    }

    public static StringBuilder appendResult(
            final StringBuilder builder,
            final Object result
    ) {
        return builder
                .append("result")
                .append("=")
                .append(result);
    }

    public static StringBuilder appendError(
            final StringBuilder builder,
            final Throwable throwable
    ) {
        return builder
                .append("errorType")
                .append("=")
                .append(throwable.getClass().getSimpleName())
                .append(", ")
                .append("errorMessage")
                .append("=")
                .append(throwable.getMessage());
    }
}
