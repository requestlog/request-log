package io.github.requestlog.apachehttpclient.support;

import io.github.requestlog.apachehttpclient.context.request.ApacheHttpClientRequestContext;
import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;


/**
 * Decorator for {@link HttpClient} to provide RequestLog capabilities.
 */
@RequiredArgsConstructor
@Slf4j
public class HttpClientRequestLogDecorator implements HttpClient {

    private final AbstractRequestLogHandler requestLogHandler;

    @Getter
    private final HttpClient httpClientTarget;

    @Override
    public HttpParams getParams() {
        return httpClientTarget.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return httpClientTarget.getConnectionManager();
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return execute(request, (HttpContext) null);
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return execute(URIUtils.extractHost(request.getURI()), request, context);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        return execute(target, request, (HttpContext) null);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {

        // Skipped, no logging is specified or current request contains retry.
        if (LogContext.THREAD_LOCAL.get() == null || RetryContext.THREAD_LOCAL.get() != null) {
            return httpClientTarget.execute(target, request, context);
        }

        try {
            HttpClientUtils.convertEntityRepeatable(request); // try to make request entity repeatable
            HttpResponse response = httpClientTarget.execute(target, request, context);
            HttpClientUtils.convertEntityRepeatable(response); // try to make response entity repeatable
            requestLogHandler.handle(new ApacheHttpClientRequestContext(LogContext.THREAD_LOCAL.get(), target, request, response));
            return response;
        } catch (Exception e) {
            requestLogHandler.handle(new ApacheHttpClientRequestContext(LogContext.THREAD_LOCAL.get(), target, request, e));
            throw e;
        }
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute(request, responseHandler, (HttpContext) null);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return execute(URIUtils.extractHost(request.getURI()), request, responseHandler, context);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute(target, request, responseHandler, (HttpContext) null);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return responseHandler.handleResponse(execute(target, request, context));
    }

}
