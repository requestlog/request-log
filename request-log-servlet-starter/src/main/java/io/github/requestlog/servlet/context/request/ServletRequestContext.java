package io.github.requestlog.servlet.context.request;

import io.github.requestlog.core.context.request.InboundRequestContext;
import io.github.requestlog.core.enums.HttpMethod;
import io.github.requestlog.core.enums.RequestContextType;
import io.github.requestlog.core.model.RequestRetryJob;
import io.github.requestlog.servlet.annotation.ReqLog;
import io.github.requestlog.servlet.support.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Request context 4 Servlet.
 */
@Slf4j
public class ServletRequestContext extends InboundRequestContext {


    private final ReqLog reqLog;
    private final HttpServletRequest request;
    private HttpServletResponse response;
    private final long beforeExecuteTimeMillis;


    public ServletRequestContext(ReqLog reqLog, RequestAttributes requestAttributes, long beforeExecuteTimeMillis) {
        this.reqLog = reqLog;
        if (requestAttributes instanceof ServletRequestAttributes) {
            this.request = ((ServletRequestAttributes) requestAttributes).getRequest();
            this.response = ((ServletRequestAttributes) requestAttributes).getResponse();
        } else {
            super.logRequestCache = false;
            super.retryRequestCache = false;
            this.request = null;
            this.response = null;
        }
        this.beforeExecuteTimeMillis = beforeExecuteTimeMillis;
    }

    public ServletRequestContext(ReqLog reqLog, RequestAttributes requestAttributes, Exception exception, long beforeExecuteTimeMillis) {
        this(reqLog, requestAttributes, beforeExecuteTimeMillis);
        super.exception = exception;
    }

    @Override
    public RequestRetryJob buildRequestRetryJob() {
        if (requestRetryJobCache != null) {
            return requestRetryJobCache;
        }

        RequestRetryJob retryJob = new RequestRetryJob();
        retryJob.setRequestLog(buildRequestLog());
        retryJob.setRetryWaitStrategy(reqLog.retryWaitStrategy());
        retryJob.setRetryInterval(reqLog.retryInterval());
        retryJob.setLastExecuteTimeMillis(beforeExecuteTimeMillis);
        retryJob.setExecuteCount(1);
        retryJob.setNextExecuteTimeMillis(reqLog.retryWaitStrategy().nextExecuteTime(1, reqLog.retryInterval()));

        return (requestRetryJobCache = retryJob);
    }


    @Override
    public RequestContextType getRequestContextType() {
        return RequestContextType.SERVLET;
    }

    @Override
    public boolean logRequest() {

        if (super.logRequestCache != null) {
            return super.logRequestCache;
        }

        if (exception == null) {
            return (super.logRequestCache = false);
        }

        if (reqLog.whenException().length == 0) {
            return (super.logRequestCache = true);
        }

        return (super.logRequestCache = Arrays.stream(reqLog.whenException()).anyMatch(expClass -> expClass.isInstance(exception)));
    }


    @Override
    public boolean retryRequest() {
        if (requestBodyCache != null) {
            return retryRequestCache;
        }
        return (super.retryRequestCache = (logRequest() && reqLog.retry()));
    }


    @Override
    public HttpMethod getRequestMethod() {
        return HttpMethod.of(request.getMethod());
    }

    @Override
    public String getRequestUrl() {
        return request.getRequestURL().toString();
    }

    @Override
    public String getRequestPath() {
        return request.getServletPath();
    }


    private Map<String, List<String>> requestHeadersCache;

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        if (requestHeadersCache != null) {
            return requestHeadersCache;
        }
        return (requestHeadersCache = ServletUtils.getRequestHeaders(request));
    }

    private String requestBodyCache;

    @Override
    public String getRequestBody() {
        if (requestBodyCache != null) {
            return requestBodyCache;
        }
        if (ServletUtils.isBodyRepeatableRead(request)) {
            return null;
        }
        try {
            return (requestBodyCache = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("getRequestBody error", e);
            return null;
        }
    }

    @Override
    public Integer getResponseCode() {
        return null;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return null;
    }

    @Override
    public String getResponseBody() {
        return null;
    }

}
