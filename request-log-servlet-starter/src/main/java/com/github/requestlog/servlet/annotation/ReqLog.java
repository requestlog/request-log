package com.github.requestlog.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * RequestLog annotation for controller methods.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqLog {

    /**
     * Record logs when encountering the specified exceptions.
     * Default is {@link Exception}.
     */
    Class<? extends Exception>[] whenException() default {};

    /**
     * Also generate retry job.
     */
    boolean retry() default false;

}
