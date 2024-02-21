package io.github.requestlog.feign.context.request;

import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.context.request.OutboundRequestContext;
import io.github.requestlog.core.enums.HttpMethod;
import io.github.requestlog.core.enums.RequestContextType;
import io.github.requestlog.feign.support.FeignUtils;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


/**
 * Request context for {@link feign.Client}
 */
@Slf4j
public class FeignRequestContext extends OutboundRequestContext {

    @Override
    public RequestContextType getRequestContextType() {
        return RequestContextType.FEIGN;
    }


    private final Request request;
    private final Response response;


    public FeignRequestContext(LogContext logContext, Request request, Exception exception) {
        super(logContext);
        super.exception = exception;
        this.request = request;
        this.response = null;
    }

    public FeignRequestContext(LogContext logContext, Request request, Response response) {
        super(logContext);
        this.request = request;
        this.response = response;
    }


    @Override
    public HttpMethod getRequestMethod() {
        return HttpMethod.of(request.httpMethod().name());
    }

    @Override
    public String getRequestUrl() {
        return request.url();
    }

    @Override
    public String getRequestPath() {
        try {
            return new URI(request.url()).getPath();
        } catch (URISyntaxException e) {
            log.warn("getRequestPath error", e);
            return null;
        }
    }

    private Map<String, List<String>> requestHeadersCache;

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        if (requestHeadersCache != null) {
            return requestHeadersCache;
        }
        return (requestHeadersCache = FeignUtils.convertHeaders(request.headers()));
    }

    private String requestBodyCache;

    @Override
    public String getRequestBody() {
        if (requestBodyCache != null) {
            return requestBodyCache;
        }
        if (request.body() == null || request.body().length == 0) {
            return null;
        }
        return (requestBodyCache = new String(request.body(), StandardCharsets.UTF_8));
    }

    @Override
    public Integer getResponseCode() {
        return response == null ? null : response.status();
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
        return (responseHeadersCache = FeignUtils.convertHeaders(response.headers()));
    }

    private String responseBodyCache;

    @Override
    public String getResponseBody() {

        if (response == null || response.body() == null) {
            return null;
        }

        // If reading a non-repeatable body, it affects the normal reading of the main program.
        if (!response.body().isRepeatable()) {
            return null;
        }

        try {
            return (responseBodyCache = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("getResponseBody error", e);
            return null;
        }
    }
}
