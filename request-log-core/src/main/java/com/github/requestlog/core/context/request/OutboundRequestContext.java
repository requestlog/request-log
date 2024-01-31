package com.github.requestlog.core.context.request;


import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.enums.RequestLogErrorType;
import com.github.requestlog.core.model.HttpRequestContextModel;
import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.support.CollectionUtils;
import com.github.requestlog.core.support.Predicates;
import com.github.requestlog.core.support.SupplierChain;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * Request context 4 outbound
 */
@RequiredArgsConstructor
public abstract class OutboundRequestContext extends BaseRequestContext {


    protected final LogContext.ContextConfig contextConfig;
    protected Exception exception;
    protected RequestLogErrorType requestLogErrorType;


    @Override
    public boolean logRequest() {

        if (super.logRequestCache != null) {
            return super.logRequestCache;
        }

        if (contextConfig == null) {
            return (super.logRequestCache = false);
        }

        if (exception != null) {
            requestLogErrorType = RequestLogErrorType.EXCEPTION;
            Predicate<Exception> exceptionPredicate = SupplierChain.of(contextConfig.getExceptionPredicate()).or(Predicates.getExceptionPredicate(getRequestContextType())).get();
            return super.logRequestCache = exceptionPredicate.test(exception);
        }


        Predicate<HttpRequestContextModel> httpResponsePredicate = SupplierChain.of(contextConfig.getHttpResponsePredicate()).or(Predicates.getResponsePredicate(getRequestContextType())).get();

        HttpRequestContextModel httpRequestContext = new HttpRequestContextModel();
        httpRequestContext.setHttpMethod(getRequestMethod());
        httpRequestContext.setRequestContextType(getRequestContextType());
        httpRequestContext.setRequestPath(getRequestPath());
        httpRequestContext.setRequestHeaders(CollectionUtils.unmodifiableMap(getRequestHeaders()));
        httpRequestContext.setRequestBody(getRequestBody());
        httpRequestContext.setResponseCode(getResponseCode());
        httpRequestContext.setResponseHeaders(CollectionUtils.unmodifiableMap(getResponseHeaders()));
        httpRequestContext.setResponseBody(getResponseBody());

        // TODO: 2024/1/31 catch predicate error
        if (super.logRequestCache = httpResponsePredicate.test(httpRequestContext)) {
            requestLogErrorType = RequestLogErrorType.RESPONSE;
        }

        return super.logRequestCache;
    }

    @Override
    public boolean retryRequest() {
        return logRequest() && Boolean.TRUE.equals(contextConfig.getRetry());
    }


    @Override
    public RequestLog buildRequestLog() {
        if (super.requestLogCache != null) {
            return super.requestLogCache;
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

        return (super.requestLogCache = requestLog);
    }


    @Override
    public RequestRetryJob buildRequestRetryJob() {
        if (super.requestRetryJobCache != null) {
            return super.requestRetryJobCache;
        }

        RequestRetryJob requestRetryJob = new RequestRetryJob();
        requestRetryJob.setRequestLog(buildRequestLog());

        // TODO: 2024/1/31 retry job fields


        return (super.requestRetryJobCache = requestRetryJob);
    }


    public abstract HttpMethod getRequestMethod();

    public abstract String getRequestUrl();

    public abstract String getRequestPath();

    public abstract Map<String, List<String>> getRequestHeaders();

    // TODO: 2024/1/31
    public String getRequestContentType() {
        return CollectionUtils.firstElement(getRequestHeaders().get("content-type"));
    }

    // TODO: 2024/1/31
    public abstract String getRequestParams();

    public abstract String getRequestBody();

    public abstract Integer getResponseCode();

    public abstract Map<String, List<String>> getResponseHeaders();

    /**
     * will query multiple times.
     * you should cache your result
     */
    public abstract String getResponseBody();

}
