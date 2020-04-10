package com.dassuncao.aspect.log.interceptor.utils;

import com.dassuncao.aspect.log.interceptor.annotation.Loggable;

public class LoggableAnnotationUtils {

    /**
     * Identifies whether or not to log function parameters.
     *
     * @param loggable loggable annotation for extracting information
     * @return true if active or false if not
     */
    public static boolean isNotActiveLogParameter(final Loggable loggable) {
        return !(loggable.enabled() && loggable.logParameters());
    }

    /**
     * Identifies whether or not to log function result.
     *
     * @param loggable loggable annotation for extracting information
     * @return true if active or false if not
     */
    public static boolean isNotActiveLogResult(final Loggable loggable) {
        return !(loggable.enabled() && loggable.logResult());
    }

    /**
     * Identifies whether or not to log error.
     *
     * @param loggable loggable annotation for extracting information
     * @return true if active or false if not
     */
    public static boolean isNotActiveLogError(final Loggable loggable) {
        return !(loggable.enabled() && loggable.logError());
    }
}
