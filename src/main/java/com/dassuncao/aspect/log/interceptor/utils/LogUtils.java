package com.dassuncao.aspect.log.interceptor.utils;

import java.lang.reflect.Method;

public class LogUtils {

    public static StringBuilder appendClass(
            final StringBuilder builder,
            final Method method
    ) {
        return builder
                .append("c=")
                .append(method.getDeclaringClass().getSimpleName())
                .append(", ");
    }

    public static StringBuilder appendMethod(
            final StringBuilder builder,
            final Method method
    ) {
        return builder
                .append("m=")
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
}
