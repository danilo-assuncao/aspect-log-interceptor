package com.dassuncao.aspect.log.interceptor.aspect;

import com.dassuncao.aspect.log.interceptor.annotation.LogParameter;
import com.dassuncao.aspect.log.interceptor.annotation.Loggable;
import com.dassuncao.aspect.log.interceptor.utils.JoinPointUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import static com.dassuncao.aspect.log.interceptor.utils.LoggableAnnotationUtils.isNotActiveLogError;
import static com.dassuncao.aspect.log.interceptor.utils.LoggableAnnotationUtils.isNotActiveLogParameter;
import static com.dassuncao.aspect.log.interceptor.utils.LoggableAnnotationUtils.isNotActiveLogResult;
import static com.dassuncao.aspect.log.interceptor.utils.StringBuilderUtils.appendClass;
import static com.dassuncao.aspect.log.interceptor.utils.StringBuilderUtils.appendError;
import static com.dassuncao.aspect.log.interceptor.utils.StringBuilderUtils.appendMethod;
import static com.dassuncao.aspect.log.interceptor.utils.StringBuilderUtils.appendParameter;
import static com.dassuncao.aspect.log.interceptor.utils.StringBuilderUtils.appendResult;

@Slf4j
@Aspect
public class LoggableAspect {

    @Before("execution(* *(..)) && @annotation(loggable)")
    public void logParameters(final JoinPoint joinPoint, final Loggable loggable) {

        if (isNotActiveLogParameter(loggable)) return;

        final var method = JoinPointUtils.getMethod(joinPoint);
        final var parameters = JoinPointUtils.getParameters(joinPoint);
        final var parameterValues = JoinPointUtils.getParameterValues(joinPoint);
        final var parameterNames = JoinPointUtils.getParameterNames(joinPoint);

        var stringBuilder = new StringBuilder();

        stringBuilder = appendClass(stringBuilder, method);
        stringBuilder = appendMethod(stringBuilder, method);

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameters[i].isAnnotationPresent(LogParameter.class)) {
                stringBuilder = appendParameter(stringBuilder, parameterNames[i], parameterValues[i]);
            }
        }

        log.info("{}", stringBuilder.toString().substring(0, stringBuilder.toString().length() - 2));
    }

    @AfterReturning(pointcut = "execution(* *(..)) && @annotation(loggable)", returning = "result")
    public void logResult(final JoinPoint joinPoint, final Object result, final Loggable loggable) {

        if (isNotActiveLogResult(loggable)) return;

        final var method = JoinPointUtils.getMethod(joinPoint);

        var stringBuilder = new StringBuilder();

        stringBuilder = appendClass(stringBuilder, method);
        stringBuilder = appendMethod(stringBuilder, method);
        stringBuilder = appendResult(stringBuilder, result);

        log.info("{}", stringBuilder.toString());
    }

    @AfterThrowing(pointcut = "execution(* *(..)) && @annotation(loggable)", throwing = "exception")
    public void logError(final JoinPoint joinPoint, final Throwable exception, final Loggable loggable) {

        if (isNotActiveLogError(loggable)) return;

        final var method = JoinPointUtils.getMethod(joinPoint);

        var stringBuilder = new StringBuilder();

        stringBuilder = appendClass(stringBuilder, method);
        stringBuilder = appendMethod(stringBuilder, method);
        stringBuilder = appendError(stringBuilder, exception);

        log.error("{}", stringBuilder.toString());
    }
}
