package com.github.requestlog.okhttp.context.request;

import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.context.request.OutboundRequestContext;
import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.enums.RequestContextType;
import com.github.requestlog.core.support.HttpUtils;
import com.github.requestlog.okhttp.support.OkHttpUtils;
import okhttp3.*;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Request context for {@link OkHttpClient}
 */
public class OkHttpRequestContext extends OutboundRequestContext {


    @Override
    public RequestContextType getRequestContextType() {
        return RequestContextType.OK_HTTP;
    }

    private final Request request;
    private Response response;

    public OkHttpRequestContext(LogContext logContext, Request request, Exception exception) {
        super(logContext);
        this.request = request;
        super.exception = exception;
    }

    public OkHttpRequestContext(LogContext logContext, Request request, Response response) {
        super(logContext);
        this.request = request;
        this.response = response;
    }

    @Override
    public HttpMethod getRequestMethod() {
        return HttpMethod.of(request.method());
    }

    @Override
    public String getRequestUrl() {
        return request.url().toString();
    }

    @Override
    public String getRequestPath() {
        // TODO: 2024/2/17 encoded path
        return request.url().encodedPath();
    }

    private Map<String, List<String>> requestHeadersCache;

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        if (requestHeadersCache != null) {
            return requestHeadersCache;
        }
        requestHeadersCache = request.headers().toMultimap();

        // Ok Http request with missing content-type situation, try get from RequestBody.
        if (getRequestMethod().supportsRequestBody()) {
            if (!StringUtils.hasText(HttpUtils.findContentType(requestHeadersCache))) {
                Optional.ofNullable(request.body())
                        .map(RequestBody::contentType).map(MediaType::toString).filter(StringUtils::hasText)
                        .ifPresent(e -> requestHeadersCache.put("Content-Type", Collections.singletonList(e)));
            }
        }
        return requestHeadersCache;
    }


    private String requestBodyCache;

    @Override
    public String getRequestBody() {
        if (requestBodyCache != null) {
            return requestBodyCache;
        }
        return (requestBodyCache = OkHttpUtils.requestBody2String(request));
    }

    @Override
    public Integer getResponseCode() {
        if (response == null) {
            return null;
        }
        return response.code();
    }

    private Map<String, List<String>> responseHeadersCache;

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        if (responseHeadersCache != null) {
            return responseHeadersCache;
        }
        if (response == null) {
            return null;
        }
        return (responseHeadersCache = response.headers().toMultimap());
    }

    private String responseBodyCache;

    @Override
    public String getResponseBody() {
        if (responseBodyCache != null) {
            return responseBodyCache;
        }
        if (!OkHttpUtils.isResponseRepeatable(response)) {
            return null;
        }

        return (responseBodyCache = OkHttpUtils.responseBody2String(response));
    }

}
