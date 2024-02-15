package com.github.requestlog.core.context;


import com.github.requestlog.core.enums.RetryWaitStrategy;
import com.github.requestlog.core.model.HttpRequestContextModel;
import com.github.requestlog.core.support.function.RunnableExp;
import com.github.requestlog.core.support.function.SupplierExp;
import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * LogContext tool
 */
public final class LogContext {

    /**
     * ThreadLocal for {@link LogContext}
     */
    public static final ThreadLocal<LogContext> THREAD_LOCAL = new InheritableThreadLocal<>();


    private LogContext() {
    }

    /**
     * log request
     */
    public static LogContext log() {
        return new LogContext();
    }

    /**
     * log request with retry job
     */
    public static LogContext retry() {
        LogContext context = new LogContext();
        context.retry = true;
        return context;
    }

    // TODO: 2024/1/31 some reserved fields like bizId、tenantId

    // TODO: 2024/2/11 bizId  tenantId as  FunctionalInterface


    /**
     * also generate retry job
     */
    @Getter
    private Boolean retry;

    @Getter
    private Integer retryInterval = 60;

    @Getter
    private RetryWaitStrategy retryWaitStrategy = RetryWaitStrategy.FIXED;

    /**
     * Custom exception predicate for current request.
     * overrides global predicates.
     */
    @Getter
    private Predicate<Exception> exceptionPredicate;

    /**
     * Custom http predicate for current request.
     * overrides global predicates.
     */
    @Getter
    private Predicate<HttpRequestContextModel> httpResponsePredicate;


    /**
     * Retry interval in seconds.
     * Only works when {@link #retry} is true.
     */
    public LogContext retryInterval(int interval) {
        assert interval > 0;
        this.retryInterval = interval;
        return this;
    }

    /**
     * Retry interval calculation strategy.
     * Only works when {@link #retry} is true.
     */
    public LogContext retryWaitStrategy(RetryWaitStrategy retryWaitStrategy) {
        assert retryWaitStrategy != null;
        this.retryWaitStrategy = retryWaitStrategy;
        return this;
    }

    /**
     * Match exception by type.
     */
    public LogContext whenException(Class<Exception>... exceptionClasses) {
        // TODO: 2024/1/27 check none null and not empty
        assert exceptionClasses != null && exceptionClasses.length > 0;
        this.exceptionPredicate = (exp) -> Arrays.stream(exceptionClasses).anyMatch(exceptionClass -> exceptionClass.isAssignableFrom(exp.getClass()));
        return this;
    }

    /**
     * Match by given {@link Predicate<Exception>}
     */
    public LogContext whenException(Predicate<Exception> exceptionPredicate) {
        this.exceptionPredicate = exceptionPredicate;
        return this;
    }

    /**
     * Match by given {@link Predicate<HttpRequestContextModel>}
     */
    // TODO: 2024/2/15 change 2 successWhenResponse?
    public LogContext whenResponse(Predicate<HttpRequestContextModel> httpResponsePredicate) {
        this.httpResponsePredicate = httpResponsePredicate;
        return this;
    }

    /**
     * execute
     * If your Runnable code declares a checked exception, then use {@link #executeWithExp(RunnableExp)}.
     */
    public void execute(Runnable runnable) {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * execute and return
     * If your Supplier code declares a checked exception, then use {@link #executeWithExp(SupplierExp)}.
     */
    public <T> T execute(Supplier<T> supplier) {
        LogContext carry = THREAD_LOCAL.get();
        try {
            THREAD_LOCAL.set(this);
            return supplier.get();
        } finally {
            if (carry == null) {
                THREAD_LOCAL.remove();
            } else {
                THREAD_LOCAL.set(carry);
            }
        }
    }


    /**
     * execute
     */
    public <E extends Exception> void executeWithExp(RunnableExp<E> runnable) throws E {
        executeWithExp(() -> {
            runnable.run();
            return null;
        });
    }


    /**
     * execute and return
     */
    public <T, E extends Exception> T executeWithExp(SupplierExp<T, E> supplier) throws E {
        final AtomicReference<Exception> exceptionCarry = new AtomicReference<>();
        final T returnObj = execute((Supplier<T>) () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                exceptionCarry.set(e);
                return null;
            }
        });
        if (exceptionCarry.get() != null) {
            try {
                @SuppressWarnings("unchecked")
                E e = (E) exceptionCarry.get();
                throw e;
            } catch (ClassCastException e) {
                throw new RuntimeException(exceptionCarry.get());
            }
        }
        return returnObj;
    }


}
