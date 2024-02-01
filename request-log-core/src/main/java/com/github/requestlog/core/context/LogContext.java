package com.github.requestlog.core.context;


import com.github.requestlog.core.model.HttpRequestContextModel;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * LogContext tool
 */
public final class LogContext {

    /**
     * ThreadLocal for {@link ContextConfig}
     */
    public static final ThreadLocal<ContextConfig> CONTEXT_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * log request
     */
    public static ContextConfig log() {
        return new ContextConfig();
    }

    /**
     * log request with retry job
     */
    public static ContextConfig retry() {
        return new ContextConfig().retry();
    }

    /**
     * Context Config
     */
    @Getter
    public static class ContextConfig {

        // TODO: 2024/1/31 some reserved fields like bizId、tenantId

        /**
         * also generate retry job
         */
        private Boolean retry;

        /**
         * Custom exception predicate for current request.
         * overrides global predicates.
         */
        private Predicate<Exception> exceptionPredicate;

        /**
         * Custom http predicate for current request.
         * overrides global predicates.
         */
        private Predicate<HttpRequestContextModel> httpResponsePredicate;

        public ContextConfig retry() {
            this.retry = true;
            return this;
        }

        /**
         * Match exception by type.
         */
        public ContextConfig whenException(Class<Exception>... exceptionClasses) {
            // TODO: 2024/1/27 check none null and not empty
            assert exceptionClasses != null && exceptionClasses.length > 0;
            this.exceptionPredicate = (exp) -> Arrays.stream(exceptionClasses).anyMatch(exceptionClass -> exceptionClass.isAssignableFrom(exp.getClass()));
            return this;
        }

        /**
         * Match by given {@link Predicate<Exception>}
         */
        public ContextConfig whenException(Predicate<Exception> exceptionPredicate) {
            this.exceptionPredicate = exceptionPredicate;
            return this;
        }

        /**
         * Match by given {@link Predicate< HttpRequestContextModel >}
         */
        public ContextConfig whenResponse(Predicate<HttpRequestContextModel> httpResponsePredicate) {
            this.httpResponsePredicate = httpResponsePredicate;
            return this;
        }

        /**
         * execute
         */
        public void execute(Runnable runnable) {
            execute(() -> {
                runnable.run();
                return null;
            });
        }

        /**
         * execute and return
         */
        public <T> T execute(Supplier<T> supplier) {
            ContextConfig carry = CONTEXT_THREAD_LOCAL.get();
            try {
                CONTEXT_THREAD_LOCAL.set(this);
                return supplier.get();
            } finally {
                if (carry == null) {
                    CONTEXT_THREAD_LOCAL.remove();
                } else {
                    CONTEXT_THREAD_LOCAL.set(carry);
                }
            }
        }

    }

}
