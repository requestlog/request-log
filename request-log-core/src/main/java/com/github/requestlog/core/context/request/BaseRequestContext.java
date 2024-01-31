package com.github.requestlog.core.context.request;

import com.github.requestlog.core.enums.RequestContextType;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.model.RequestLog;


/**
 * Base class for request contexts.
 */
public abstract class BaseRequestContext {

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
    public abstract RequestLog buildRequestLog();

    /**
     * Build {@link RequestRetryJob}
     */
    public abstract RequestRetryJob buildRequestRetryJob();


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

}
