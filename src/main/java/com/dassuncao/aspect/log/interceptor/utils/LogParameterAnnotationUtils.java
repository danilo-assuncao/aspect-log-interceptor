package com.dassuncao.aspect.log.interceptor.utils;

import com.dassuncao.aspect.log.interceptor.annotation.LogParameter;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

@UtilityClass
public class LogParameterAnnotationUtils {

    public boolean hasLogParameterAnnotation(final Parameter parameter) {
        for (final Annotation annotation : parameter.getAnnotations()) {
            if (LogParameter.class.equals(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }
}
