package io.github.requestlog.core.context.request;


import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.enums.RequestLogErrorType;
import io.github.requestlog.core.enums.RetryWaitStrategy;
import io.github.requestlog.core.model.HttpRequestContext;
import io.github.requestlog.core.model.RequestLog;
import io.github.requestlog.core.model.RequestRetryJob;
import io.github.requestlog.core.support.CollectionUtils;
import io.github.requestlog.core.support.Predicates;
import io.github.requestlog.core.support.SupplierChain;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Predicate;


/**
 * Request context 4 outbound
 */
@RequiredArgsConstructor
public abstract class OutboundRequestContext extends BaseRequestContext {


    protected final LogContext logContext;


    @Override
    public boolean logRequest() {

        if (super.logRequestCache != null) {
            return super.logRequestCache;
        }

        if (logContext == null) {
            return (super.logRequestCache = false);
        }

        if (exception != null) {
            requestLogErrorType = RequestLogErrorType.EXCEPTION;

            Predicate<Exception> ignoreExceptionPredicate = SupplierChain.of(logContext.getIgnoreExceptionPredicate())
                    .or(Predicates.getIgnoreExceptionPredicate(getRequestContextType())).get();
            return super.logRequestCache = !ignoreExceptionPredicate.test(exception);
        }


        Predicate<HttpRequestContext> successHttpResponsePredicate = SupplierChain.of(logContext.getSuccessHttpResponsePredicate())
                .or(Predicates.getSuccessHttpResponsePredicate(getRequestContextType())).get();

        // TODO: 2024/1/31 catch predicate error
        if (super.logRequestCache = !successHttpResponsePredicate.test(buildHttpRequestContext())) {
            requestLogErrorType = RequestLogErrorType.RESPONSE;
        }

        return super.logRequestCache;
    }

    @Override
    public boolean retryRequest() {
        if (retryRequestCache != null) {
            return retryRequestCache;
        }
        return (retryRequestCache = (logRequest() && Boolean.TRUE.equals(logContext.getRetry())));
    }


    @Override
    public RequestLog buildRequestLog() {
        RequestLog requestLog = super.buildRequestLog();
        requestLog.setAttributeMap(logContext.getAttributeMap());
        return requestLog;
    }


    @Override
    public RequestRetryJob buildRequestRetryJob() {
        if (super.requestRetryJobCache != null) {
            return super.requestRetryJobCache;
        }

        RequestRetryJob retryJob = new RequestRetryJob();
        retryJob.setRequestLog(buildRequestLog());
        retryJob.setRetryWaitStrategy(Optional.ofNullable(logContext.getRetryWaitStrategy()).orElse(RetryWaitStrategy.FIXED));
        retryJob.setRetryInterval(Optional.ofNullable(logContext.getRetryInterval()).orElse(60));
        retryJob.setLastExecuteTimeMillis(logContext.getBeforeExecuteTimeMillis());
        retryJob.setExecuteCount(1);
        retryJob.setNextExecuteTimeMillis(retryJob.getRetryWaitStrategy().nextExecuteTime(1, retryJob.getRetryInterval()));
        retryJob.setMaxExecuteCount(logContext.getMaxExecuteCount());

        return (super.requestRetryJobCache = retryJob);
    }


    // TODO: 2024/2/12 super?
    public HttpRequestContext buildHttpRequestContext() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.setHttpMethod(getRequestMethod());
        requestContext.setRequestContextType(getRequestContextType());
        requestContext.setRequestUrl(getRequestUrl());
        requestContext.setRequestPath(getRequestPath());
        requestContext.setRequestHeaders(CollectionUtils.unmodifiableMap(getRequestHeaders()));
        requestContext.setRequestBody(getRequestBody());
        requestContext.setResponseCode(getResponseCode());
        requestContext.setResponseHeaders(CollectionUtils.unmodifiableMap(getResponseHeaders()));
        requestContext.setResponseBody(getResponseBody());
        return requestContext;
    }

}
