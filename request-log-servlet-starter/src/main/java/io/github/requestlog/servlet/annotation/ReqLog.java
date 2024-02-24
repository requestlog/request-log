package io.github.requestlog.servlet.annotation;

import io.github.requestlog.core.enums.RetryWaitStrategy;

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

    /**
     * Retry interval calculation strategy.
     * Only works when {@link #retry} is true.
     */
    RetryWaitStrategy retryWaitStrategy() default RetryWaitStrategy.FIXED;

    /**
     * Retry interval in seconds.
     * Only works when {@link #retry} is true.
     */
    int retryInterval() default 60;

}
