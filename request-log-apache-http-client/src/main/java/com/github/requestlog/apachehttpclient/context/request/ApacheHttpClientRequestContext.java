package com.github.requestlog.apachehttpclient.context.request;

import com.github.requestlog.apachehttpclient.support.HttpClientUtils;
import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.context.request.OutboundRequestContext;
import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.enums.RequestContextType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


/**
 * Request context for {@link HttpClient}.
 */
@Slf4j
public class ApacheHttpClientRequestContext extends OutboundRequestContext {


    @Override
    public RequestContextType getRequestContextType() {
        return RequestContextType.APACHE_HTTP_CLIENT;
    }


    private final HttpHost host;
    private final HttpRequest request;
    private final URI uri;

    @Nullable
    private HttpResponse response;


    private ApacheHttpClientRequestContext(LogContext.ContextConfig contextConfig, HttpHost host, HttpRequest request) {
        super(contextConfig);
        this.host = host;
        this.request = request;
        this.uri = HttpClientUtils.buildURI(host, request);
    }

    public ApacheHttpClientRequestContext(LogContext.ContextConfig contextConfig, HttpHost host, HttpRequest request, Exception exception) {
        this(contextConfig, host, request);
        super.exception = exception;
    }

    public ApacheHttpClientRequestContext(LogContext.ContextConfig contextConfig, HttpHost host, HttpRequest request, HttpResponse response) {
        this(contextConfig, host, request);
        this.response = response;
    }


    @Override
    public HttpMethod getRequestMethod() {
        return HttpMethod.of(request.getRequestLine().getMethod());
    }

    @Override
    public String getRequestUrl() {
        return uri.toString();
    }

    @Override
    public String getRequestPath() {
        return uri.getPath();
    }


    private Map<String, List<String>> requestHeadersCache;

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        if (requestHeadersCache != null) {
            return requestHeadersCache;
        }
        return (requestHeadersCache = HttpClientUtils.convertHeaders(request.getAllHeaders()));
    }

    @Override
    public String getRequestParams() {
        // TODO: 2024/2/7
        return null;
    }


    private String requestBodyCache;

    @Override
    public String getRequestBody() {

        if (requestBodyCache != null) {
            return requestBodyCache;
        }

        if (!(request instanceof HttpEntityEnclosingRequest)) {
            return null;
        }

        HttpEntity httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
        if (httpEntity == null || !httpEntity.isRepeatable()) {
            return null;
        }

        try {
            return (requestBodyCache = StreamUtils.copyToString(httpEntity.getContent(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("getRequestBody error", e);
            return null;
        }
    }

    @Override
    public Integer getResponseCode() {
        if (response == null) {
            return null;
        }
        return response.getStatusLine().getStatusCode();
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
        return (responseHeadersCache = HttpClientUtils.convertHeaders(response.getAllHeaders()));
    }


    private String responseBodyCache;

    @Override
    public String getResponseBody() {
        if (responseBodyCache != null) {
            return responseBodyCache;
        }
        if (response == null || response.getEntity() == null || !response.getEntity().isRepeatable()) {
            return null;
        }
        try {
            return responseBodyCache = StreamUtils.copyToString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("getResponseBody error", e);
            return null;
        }
    }

}
