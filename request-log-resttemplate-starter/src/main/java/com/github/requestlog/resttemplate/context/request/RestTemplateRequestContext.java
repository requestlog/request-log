package com.github.requestlog.resttemplate.context.request;

import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.context.request.OutboundRequestContext;
import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.enums.RequestContextType;
import com.github.requestlog.resttemplate.support.RestTemplateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


/**
 * Request context for {@link org.springframework.web.client.RestTemplate}
 */
@Slf4j
public class RestTemplateRequestContext extends OutboundRequestContext {


    @Override
    public RequestContextType getRequestContextType() {
        return RequestContextType.REST_TEMPLATE;
    }


    private final HttpRequest request;
    private final byte[] body;

    @Nullable
    private final ClientHttpResponse response;


    public RestTemplateRequestContext(LogContext.ContextConfig contextConfig, HttpRequest request, byte[] body, Exception exception) {
        super(contextConfig);
        super.exception = exception;
        this.request = request;
        this.body = body;
        this.response = null;
    }

    public RestTemplateRequestContext(LogContext.ContextConfig contextConfig, HttpRequest request, byte[] body, @Nullable ClientHttpResponse response) {
        super(contextConfig);
        this.request = request;
        this.body = body;
        this.response = response;
    }


    @Override
    public HttpMethod getRequestMethod() {
        return HttpMethod.of(request.getMethodValue());
    }


    @Override
    public String getRequestUrl() {
        return request.getURI().toString();
    }

    @Override
    public String getRequestPath() {
        // TODO: 2024/1/27 get raw path
        return request.getURI().getPath();
    }

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        return request.getHeaders();
    }

    @Override
    public String getRequestParams() {
        return null;
    }

    private String bodyCache;

    @Override
    public String getRequestBody() {
        if (bodyCache != null) {
            return bodyCache;
        }
        return (bodyCache = (body == null ? null : new String(body, StandardCharsets.UTF_8)));
    }

    @Override
    public Integer getResponseCode() {
        try {
            return response == null ? null : response.getRawStatusCode();
        } catch (IOException e) {
            log.warn("getResponseCode error", e);
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return response == null ? null : response.getHeaders();
    }

    /**
     * Cache 4 {@link #getRequestBody()}
     */
    private String responseBodyCache;

    @Override
    public String getResponseBody() {
        if (responseBodyCache != null) {
            return responseBodyCache;
        }

        if (response == null || !RestTemplateUtils.isRepeatableBody(response)) {
            return null;
        }

        try {
            return (responseBodyCache = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("getResponseBody error", e);
            return null;
        }
    }
}
