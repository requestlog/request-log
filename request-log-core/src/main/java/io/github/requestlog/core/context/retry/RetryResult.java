package io.github.requestlog.core.context.retry;


import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.enums.RetryClientType;
import io.github.requestlog.core.model.HttpRequestContext;
import io.github.requestlog.core.model.RequestRetryJob;
import io.github.requestlog.core.model.RequestRryLog;
import io.github.requestlog.core.support.HttpUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;


/**
 * Retry result
 */
@Slf4j
@Getter
public class RetryResult {

    public RetryResult(RetryClientType retryClientType, long executeTimeMillis, RetryContext retryContext, HttpRequestContext requestContext) {
        this(retryClientType, executeTimeMillis, retryContext, requestContext, null);
    }

    public RetryResult(RetryClientType retryClientType, long executeTimeMillis, RetryContext retryContext, HttpRequestContext requestContext, Exception exception) {
        this.retryClientType = retryClientType;
        this.executeTimeMillis = executeTimeMillis;
        this.retryContext = retryContext;
        this.requestContext = requestContext;
        this.exception = exception;
    }


    @Getter
    private final RetryClientType retryClientType;

    /**
     * Time millis before execute
     */
    @Getter
    private final long executeTimeMillis;

    @Getter
    private final RetryContext retryContext;

    /**
     * Whether an exception occurred during the request process.
     */
    @Getter
    private final Exception exception;

    @Getter
    private final HttpRequestContext requestContext;


    private Boolean succeedCache;

    /**
     * Checks current retry succeed.
     */
    public boolean succeed() {
        if (succeedCache != null) {
            return succeedCache;
        }

        if (exception != null) {
            return (succeedCache = false);
        }

        // custom response predicate
        if (retryContext.getSuccessHttpResponsePredicate() != null) {
            return (succeedCache = retryContext.getSuccessHttpResponsePredicate().test(requestContext));
        }

        // default response predicate only checks response code is 2xx
        return (succeedCache = HttpUtils.isSuccess(requestContext.getResponseCode()));
    }


    /**
     * Indicates whether {@link #updateRetryJob} has been invoked.
     */
    private Boolean updateRetryJobInvoked;

    /**
     * Updates the {@link RequestRetryJob} passed when creating the {@link RetryContext}.
     * Modifies the original object.
     */
    public RequestRetryJob updateRetryJob() {

        if (retryContext.getRequestRetryJob() == null) {
            log.warn("When initializing Retry Context, no RequestRetryJob object was provided. If you want to generate a new job, new RequestRetryJob can be created using IRequestLogRepository#generateNewRetryJob.");
            return null;
        }

        if (Boolean.TRUE.equals(updateRetryJobInvoked)) {
            return retryContext.getRequestRetryJob();
        }

        RequestRetryJob retryJob = retryContext.getRequestRetryJob();
        retryJob.setLastExecuteTimeMillis(executeTimeMillis); // before http request time.
        retryJob.setExecuteCount(retryJob.getExecuteCount() + 1);
        //  The next execution time is calculated based on the current time to prevent the time interval from becoming ineffective due to prolonged request times.
        retryJob.setNextExecuteTimeMillis(retryJob.getRetryWaitStrategy().nextExecuteTime(retryJob.getExecuteCount(), retryJob.getRetryInterval()));
        updateRetryJobInvoked = true;

        return retryJob;
    }


    private RequestRryLog requestRryLogCache = null;

    /**
     * Generates a log for the current retry.
     */
    public RequestRryLog generateRetryLog() {

        if (requestRryLogCache != null) {
            return requestRryLogCache;
        }

        RequestRryLog retryLog = new RequestRryLog();

        retryLog.setRequestLog(retryContext.getRequestLog());
        retryLog.setRequestRetryJob(retryContext.getRequestRetryJob());

        retryLog.setRetryClientType(retryClientType);
        retryLog.setSucceed(succeed());
        retryLog.setExecuteCount(Optional.ofNullable(retryContext.getRequestRetryJob()).map(RequestRetryJob::getExecuteCount).orElse(1)); // TODO: 2024/2/14 retry job object maybe updated
        retryLog.setExecuteTimeMillis(executeTimeMillis);

        retryLog.setException(exception);
        retryLog.setRequestUrl(requestContext.getRequestUrl());
        retryLog.setRequestHeaders(requestContext.getRequestHeaders());
        retryLog.setRequestBody(requestContext.getRequestBody());
        retryLog.setResponseCode(requestContext.getResponseCode());
        retryLog.setResponseHeaders(requestContext.getResponseHeaders());
        retryLog.setResponseBody(requestContext.getResponseBody());

        return (requestRryLogCache = retryLog);
    }

}
