package com.dassuncao.aspect.log.interceptor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {

    /**
     * Enables the log function for the target method.
     *
     * @return true if active and false if inactive.
     */
    boolean enabled() default true;

    /**
     * Activates the functionality of logging method error or not.
     *
     * @return true if active and false if inactive.
     */
    boolean logError() default true;

    /**
     * Activates the functionality of logging method parameters or not.
     *
     * @return true if active and false if inactive.
     */
    boolean logParameters() default true;
}
