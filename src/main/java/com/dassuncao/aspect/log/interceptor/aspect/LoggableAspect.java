package com.dassuncao.aspect.log.interceptor.aspect;

import com.dassuncao.aspect.log.interceptor.annotation.Loggable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
public class LoggableAspect {

    @Before("execution(* *(..)) && @annotation(loggable)")
    public void logParameters(final JoinPoint joinPoint, final Loggable loggable) {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        final Method method = methodSignature.getMethod();

        for (final Parameter parameter : method.getParameters()) {
            log.info("{}", parameter.getName());
        }




//        for (int i = 0; i < parameterAnnotations.length; i++) {
//            Annotation[] annotations = parameterAnnotations[i];
//            for (Annotation annotation : annotations) {
//                System.out.println(annotation);
//                if (annotation.annotationType() == LogParameter.class) {
//                    System.out.println("PARAM = " + args[i] + " | Annotation = " + ((LogParameter) annotation).annotationType());
//                }
//            }
//        }
    }

//    @Around("execution(* *(..)) && @annotation(loggable)")
//    public Object around(final ProceedingJoinPoint proceedingJoinPoint, final Loggable loggable) throws Throwable {
//        Object[] args = proceedingJoinPoint.getArgs();
//        Arrays.stream(args).forEach(it -> {
//            log.info("args={}", it);
//            log.info("annotation={}", it.getClass().getAnnotations().length);
//        });
//        return proceedingJoinPoint.proceed();
//    }

    @AfterReturning(pointcut = "execution(* *(..)) && @annotation(loggable)", returning = "returnedValue")
    public void logResult(final Object returnedValue, final Loggable loggable) {
        log.info("toggle={}", loggable.enabled());
        log.info("return={}", returnedValue);
    }

    @AfterThrowing(pointcut = "execution(* *(..)) && @annotation(loggable)", throwing = "exception")
    public void logError(final Throwable exception, final Loggable loggable) {
        log.info("exception={}", exception.getMessage());
    }
}
