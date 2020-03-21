package com.dassuncao.aspect.log.interceptor.utils;

import com.dassuncao.aspect.log.interceptor.annotation.Loggable;

public class LoggableAnnotationUtils {

    public static boolean isNotActiveLogParameter(final Loggable loggable) {
        return !(loggable.enabled() && loggable.logParameters());
    }

    public static boolean isNotActiveLogResult(final Loggable loggable) {
        return !(loggable.enabled() && loggable.logResult());
    }

    public static boolean isNotActiveLogError(final Loggable loggable) {
        return !(loggable.enabled() && loggable.logError());
    }
}
