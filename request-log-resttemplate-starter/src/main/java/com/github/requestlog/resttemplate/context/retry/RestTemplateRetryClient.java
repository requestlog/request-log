package com.github.requestlog.resttemplate.context.retry;

import com.github.requestlog.core.context.RetryContext;
import com.github.requestlog.core.context.retry.RetryClient;
import com.github.requestlog.core.context.retry.RetryResult;
import com.github.requestlog.core.enums.RetryClientType;
import com.github.requestlog.resttemplate.context.request.RestTemplateRequestContext;
import com.github.requestlog.resttemplate.support.RestTemplateUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicReference;

import static com.github.requestlog.core.constant.Constants.RETRY_HEADER;


/**
 * RetryClient for {@link RestTemplate}
 */
public class RestTemplateRetryClient extends RetryClient<RestTemplate> {

    static {
        RetryClient.NEW_INSTANCE_MAP.put(RestTemplateRetryClient.class, RestTemplateRetryClient::new);
    }

    public RestTemplateRetryClient(RetryContext retryContext) {
        super(retryContext);
    }


    // TODO: 2024/2/12 check before execute request
    private void checkBeforeExecute() {
        if (httpClient == null) {
            throw new IllegalArgumentException("A restTemplate is required to be specified as the client.");
        }
        // TODO: 2024/2/12 check retry context
        super.validContext();
    }


    /**
     * Perform request
     */
    protected RetryResult doExecute() {

        checkBeforeExecute();

        HttpHeaders headers = RestTemplateUtils.convert2HttpHeaders(retryContext.buildRequestHeaders());
        headers.add(RETRY_HEADER, generateRetryHeaderValue());

        HttpEntity<String> requestEntity = new HttpEntity<>(retryContext.buildRequestBody(), headers);

        AtomicReference<ClientHttpRequest> requestHolder = new AtomicReference<>();
        RetryResult result = null;
        try {
            ClientHttpResponse response = httpClient.execute(retryContext.buildRequestUrl(), HttpMethod.valueOf(retryContext.getRequestLog().getHttpMethod().name()), (clientHttpRequest) -> {
                requestHolder.set(clientHttpRequest);
                httpClient.httpEntityCallback(requestEntity).doWithRequest(clientHttpRequest);
            }, (ResponseExtractor<ClientHttpResponse>) e -> e);
            result = new RetryResult(RetryClientType.REST_TEMPLATE, beforeDoExecuteTimeMillis, retryContext, new RestTemplateRequestContext(null, requestHolder.get(), null, response).buildHttpRequestContext());
        } catch (Exception e) {
            result = new RetryResult(RetryClientType.REST_TEMPLATE, beforeDoExecuteTimeMillis, retryContext, new RestTemplateRequestContext(null, requestHolder.get(), null, e).buildHttpRequestContext(), e);
        }

        return result;
    }

}
