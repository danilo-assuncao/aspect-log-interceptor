package com.dassuncao.aspect.log.interceptor.aspect;

import com.dassuncao.aspect.log.interceptor.utils.JoinPointUtils;
import com.dassuncao.aspect.log.interceptor.utils.LogUtils;
import com.dassuncao.aspect.log.interceptor.annotation.LogParameter;
import com.dassuncao.aspect.log.interceptor.annotation.Loggable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Slf4j
@Aspect
public class LoggableAspect {

    @Before("execution(* *(..)) && @annotation(loggable)")
    public void logParameters(final JoinPoint joinPoint, final Loggable loggable) {

        final var method = JoinPointUtils.getMethod(joinPoint);
        final var parameters = JoinPointUtils.getParameters(joinPoint);
        final var parameterValues = JoinPointUtils.getParameterValues(joinPoint);
        final var parameterNames = JoinPointUtils.getParameterNames(joinPoint);

        var stringBuilder = new StringBuilder();

        stringBuilder = LogUtils.appendClass(stringBuilder, method);
        stringBuilder = LogUtils.appendMethod(stringBuilder, method);

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameters[i].isAnnotationPresent(LogParameter.class)) {
                stringBuilder = LogUtils.appendParameter(stringBuilder, parameterNames[i], parameterValues[i]);
            }
        }

        log.info("{}", stringBuilder.toString());
    }

//    @AfterReturning(pointcut = "execution(* *(..)) && @annotation(loggable)", returning = "returnedValue")
//    public void logResult(final JoinPoint joinPoint, final Object returnedValue, final Loggable loggable) {
//        log.info("toggle={}", loggable.enabled());
//        log.info("return={}", returnedValue);
//    }
//
//    @AfterThrowing(pointcut = "execution(* *(..)) && @annotation(loggable)", throwing = "exception")
//    public void logError(final Throwable exception, final Loggable loggable) {
//        log.info("exception={}", exception.getMessage());
//    }
}
