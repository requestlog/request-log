package io.github.requestlog.apachehttpclient.context.retry;

import io.github.requestlog.apachehttpclient.context.request.ApacheHttpClientRequestContext;
import io.github.requestlog.apachehttpclient.support.HttpClientUtils;
import io.github.requestlog.apachehttpclient.support.client.methods.CustomNonBodyHttpRequest;
import io.github.requestlog.apachehttpclient.support.client.methods.CustomWithBodyHttpRequest;
import io.github.requestlog.core.constant.Constants;
import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.context.retry.RetryClient;
import io.github.requestlog.core.context.retry.RetryResult;
import io.github.requestlog.core.enums.HttpMethod;
import io.github.requestlog.core.enums.RetryClientType;
import io.github.requestlog.core.support.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import java.net.URI;


/**
 * RetryClient for {@link HttpClient}
 */
@Slf4j
public class ApacheHttpClientRetryClient extends RetryClient<HttpClient> {

    static {
        RetryClient.NEW_INSTANCE_MAP.put(ApacheHttpClientRetryClient.class, ApacheHttpClientRetryClient::new);
    }

    public ApacheHttpClientRetryClient(RetryContext retryContext) {
        super(retryContext);
    }


    @Override
    protected RetryResult doExecute() {

        Preconditions.check(httpClient != null, "httpClient is null");
        Preconditions.check(super.validContext(), "retryContext is not valid for retry");

        HttpMethod method = retryContext.getRequestLog().getHttpMethod();

        URI uri = URI.create(retryContext.buildRequestUrl());
        HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

        HttpRequest request = null;
        if (!method.supportsRequestBody()) {
            request = new CustomNonBodyHttpRequest(method.name(), uri);
        } else {
            request = new CustomWithBodyHttpRequest(method.name(), uri, retryContext.getRequestLog().getRequestBody());
        }

        request.setHeaders(HttpClientUtils.convertToHeaders(retryContext.buildRequestHeaders()));
        request.addHeader(new BasicHeader(Constants.RETRY_HEADER, generateRetryHeaderValue()));

        RetryResult result = null;
        try {
            HttpClientUtils.convertEntityRepeatable(request);
            HttpResponse response = httpClient.execute(httpHost, request);
            HttpClientUtils.convertEntityRepeatable(response);
            result = new RetryResult(RetryClientType.APACHE_HTTP_CLIENT, beforeDoExecuteTimeMillis, retryContext, new ApacheHttpClientRequestContext(null, httpHost, request, response).buildHttpRequestContext());
        } catch (Exception e) {
            result = new RetryResult(RetryClientType.APACHE_HTTP_CLIENT, beforeDoExecuteTimeMillis, retryContext, new ApacheHttpClientRequestContext(null, httpHost, request, e).buildHttpRequestContext());
        }

        return result;
    }

}
