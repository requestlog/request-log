package io.github.requestlog.core.context.request;

import io.github.requestlog.core.enums.RequestContextType;
import io.github.requestlog.core.enums.RequestLogErrorType;
import io.github.requestlog.core.model.RequestLog;
import io.github.requestlog.core.model.RequestRetryJob;


/**
 * Base class for request contexts.
 */
public abstract class BaseRequestContext implements HttpRequestContext {

    /**
     * Request client type.
     *
     * @see RequestContextType
     */
    public abstract RequestContextType getRequestContextType();

    /**
     * Return if current request should be logged.
     */
    public abstract boolean logRequest();

    /**
     * Return if current request should also generate retry job.
     *
     * Only works when {@link #logRequest()} is true.
     */
    public abstract boolean retryRequest();


    /**
     * Build {@link RequestLog}
     */
    public RequestLog buildRequestLog() {
        if (requestLogCache != null) {
            return requestLogCache;
        }
        RequestLog requestLog = new RequestLog();
        requestLog.setContextType(getRequestContextType());
        requestLog.setLogErrorType(requestLogErrorType);

        // exception
        requestLog.setException(exception);

        // request
        requestLog.setHttpMethod(getRequestMethod());
        requestLog.setRequestUrl(getRequestUrl());
        requestLog.setRequestPath(getRequestPath());
        requestLog.setRequestHeaders(getRequestHeaders());
        requestLog.setRequestBody(getRequestBody());

        // response
        requestLog.setResponseCode(getResponseCode());
        requestLog.setResponseHeaders(getResponseHeaders());
        requestLog.setResponseBody(getResponseBody());

        return (requestLogCache = requestLog);
    }

    /**
     * Build {@link RequestRetryJob}
     */
    public RequestRetryJob buildRequestRetryJob() {
        if (requestRetryJobCache != null) {
            return requestRetryJobCache;
        }

        RequestRetryJob requestRetryJob = new RequestRetryJob();
        requestRetryJob.setRequestLog(buildRequestLog());

        // TODO: 2024/1/31 retry job fields


        return (requestRetryJobCache = requestRetryJob);
    }


    /**
     * Cache for {@link #logRequest()}
     */
    protected Boolean logRequestCache;

    /**
     * Cache for {@link #retryRequest()}
     */
    protected Boolean retryRequestCache;

    /**
     * Cache for {@link #buildRequestLog()}
     */
    protected RequestLog requestLogCache;

    /**
     * Cache for {@link #buildRequestRetryJob()}
     */
    protected RequestRetryJob requestRetryJobCache;


    /**
     * Exception for the current request context.
     */
    protected Exception exception;

    /**
     * Error type for the current request context.
     *
     * This value will be evaluated and assigned in the first invocation of the {@link #logRequest} method.
     */
    protected RequestLogErrorType requestLogErrorType;

}
