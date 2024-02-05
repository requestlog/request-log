package com.github.requestlog.core.context.request;


import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.enums.RequestLogErrorType;
import com.github.requestlog.core.model.HttpRequestContextModel;
import com.github.requestlog.core.support.CollectionUtils;
import com.github.requestlog.core.support.Predicates;
import com.github.requestlog.core.support.SupplierChain;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;


/**
 * Request context 4 outbound
 */
@RequiredArgsConstructor
public abstract class OutboundRequestContext extends BaseRequestContext {


    protected final LogContext.ContextConfig contextConfig;


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
        if (retryRequestCache != null) {
            return retryRequestCache;
        }
        return (retryRequestCache = (logRequest() && Boolean.TRUE.equals(contextConfig.getRetry())));
    }

}
