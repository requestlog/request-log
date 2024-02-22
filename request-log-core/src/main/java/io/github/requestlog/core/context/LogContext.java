package io.github.requestlog.core.context;


import io.github.requestlog.core.enums.RetryWaitStrategy;
import io.github.requestlog.core.model.HttpRequestContext;
import io.github.requestlog.core.support.function.RunnableExp;
import io.github.requestlog.core.support.function.SupplierExp;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


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
     * Returns true if the {@link Exception} occurred but still considered successful.
     */
    @Getter
    private Predicate<Exception> ignoreExceptionPredicate;

    /**
     * Custom http predicate for current request.
     * overrides global predicates.
     * Returns true if the response is considered successful.
     */
    @Getter
    private Predicate<HttpRequestContext> successHttpResponsePredicate;


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
     * Ignore given {@link Exception} types, still consider them as successful when these Exceptions occurred.
     */
    @SafeVarargs
    public final LogContext ignoreException(Class<? extends Exception> ignoreException, Class<? extends Exception>... moreIgnoreExceptions) {
        this.ignoreExceptionPredicate = (exp) -> Stream.concat(Stream.of(ignoreException), Stream.of(moreIgnoreExceptions)).noneMatch(exceptionClass -> exceptionClass.isAssignableFrom(exp.getClass()));
        return this;
    }

    /**
     * Predicate that returns true if the {@link Exception} occurred but still considered successful.
     */
    public LogContext ignoreException(Predicate<Exception> ignoreExceptionPredicate) {
        this.ignoreExceptionPredicate = ignoreExceptionPredicate;
        return this;
    }

    /**
     * Checks if the response is successful.
     * When no custom Predicate is specified, it defaults to checking if the http status code is 2xx.
     *
     * @param successHttpResponsePredicate Predicate that returns true if the response is considered successful.
     */
    public LogContext successWhenResponse(Predicate<HttpRequestContext> successHttpResponsePredicate) {
        this.successHttpResponsePredicate = successHttpResponsePredicate;
        return this;
    }


    /**
     * Time millis before {@link #execute(Supplier)}
     */
    @Getter
    private long beforeExecuteTimeMillis;

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
            beforeExecuteTimeMillis = System.currentTimeMillis();
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
