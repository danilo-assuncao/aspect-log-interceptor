package com.dassuncao.aspect.log.interceptor.utils;

import java.lang.reflect.Method;

public class StringBuilderUtils {

    /**
     * Concatenates the log with class information to the builder.
     *
     * @param builder @see {@link StringBuilder}
     * @param method  method information
     * @return StringBuilder with information for logging class data
     */
    public static StringBuilder appendClass(
            final StringBuilder builder,
            final Method method
    ) {
        return builder
                .append("class=")
                .append(method.getDeclaringClass().getSimpleName())
                .append(", ");
    }

    /**
     * Concatenates the log with method to the builder.
     *
     * @param builder @see {@link StringBuilder}
     * @param method  method information
     * @return StringBuilder with information for logging method
     */
    public static StringBuilder appendMethod(
            final StringBuilder builder,
            final Method method
    ) {
        return builder
                .append("method=")
                .append(method.getName())
                .append(", ");
    }

    /**
     * Concatenates the log with method parameter information to the builder.
     *
     * @param builder        @see {@link StringBuilder}
     * @param parameterName  method parameter name
     * @param parameterValue method parameter value
     * @return StringBuilder with information for logging method parameter
     */
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

    /**
     * Concatenates the log with method result to the builder.
     *
     * @param builder @see {@link StringBuilder}
     * @param result  method result
     * @return StringBuilder with information for logging method result
     */
    public static StringBuilder appendResult(
            final StringBuilder builder,
            final Object result
    ) {
        return builder
                .append("result")
                .append("=")
                .append(result);
    }

    /**
     * Concatenates the log with error information information to the builder.
     *
     * @param builder   @see {@link StringBuilder}
     * @param throwable error information
     * @return StringBuilder with information for logging error
     */
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
