package com.dassuncao.aspect.log.interceptor.utils;

import com.dassuncao.aspect.log.interceptor.annotation.Loggable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LoggableAnnotationUtils {

    public boolean isActiveLog(final Loggable loggable) {
        return loggable.enabled();
    }

    public boolean isActiveLogError(final Loggable loggable) {
        return loggable.logError();
    }

    public boolean isActiveLogParameters(final Loggable loggable) {
        return loggable.logParameters();
    }
}
