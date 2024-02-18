package com.github.requestlog.core.context.retry;

import com.github.requestlog.core.context.RetryContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * @param <C> The type of Http client.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class RetryClient<C> {


    public static final Map<Class<? extends RetryClient<?>>, Function<RetryContext, RetryClient<?>>> NEW_INSTANCE_MAP = new HashMap<>();


    protected final RetryContext retryContext;

    protected C httpClient;

    public void setHttpClient(C httpClient) {
        this.httpClient = httpClient;
    }


    public RetryResult execute() {
        RetryContext carry = RetryContext.THREAD_LOCAL.get();
        try {
            RetryContext.THREAD_LOCAL.set(retryContext);
            return doExecute();
        } finally {
            if (carry == null) {
                RetryContext.THREAD_LOCAL.remove();
            } else {
                RetryContext.THREAD_LOCAL.set(carry);
            }
        }
    }

    protected abstract RetryResult doExecute();


    protected String generateRetryHeaderValue() {
        // TODO: 2024/2/18 generate retry header from retryContext.requestLog retryContext.requestRetryJob(may be null)
        //  maybe define some method like #getId、#getId4Retry
        return "123";
    }


    // TODO: 2024/2/14 checks if the retryContext executable
    protected boolean validContext() {
        // TODO: 2024/2/11
        return false;
    }


}
