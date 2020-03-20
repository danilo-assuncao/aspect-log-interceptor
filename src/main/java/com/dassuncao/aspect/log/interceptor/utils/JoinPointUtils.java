package com.dassuncao.aspect.log.interceptor.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class JoinPointUtils {

    /**
     * Gets the name of all parameters of the intercepted method via AspectJ JoinPoint.
     * @param joinPoint @see {@link JoinPoint}
     * @return Name of the intercepted method parameters
     */
    public static Object[] getParameterValues(final JoinPoint joinPoint) {
        return joinPoint.getArgs();
    }

    /**
     * Gets the value of all parameters of the intercepted method via AspectJ JoinPoint.
     * @param joinPoint @see {@link JoinPoint}
     * @return Value of the intercepted method parameters
     */
    public static String[] getParameterNames(final JoinPoint joinPoint) {
        final var methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getParameterNames();
    }

    public static Method getMethod(final JoinPoint joinPoint) {
        final var methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    public static Parameter[] getParameters(final JoinPoint joinPoint) {
        final var methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod().getParameters();
    }
}
