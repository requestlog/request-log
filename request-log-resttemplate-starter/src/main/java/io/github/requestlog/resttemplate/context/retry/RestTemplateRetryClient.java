package io.github.requestlog.resttemplate.context.retry;

import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.context.retry.RetryClient;
import io.github.requestlog.core.context.retry.RetryResult;
import io.github.requestlog.core.enums.RetryClientType;
import io.github.requestlog.core.support.Preconditions;
import io.github.requestlog.resttemplate.context.request.RestTemplateRequestContext;
import io.github.requestlog.resttemplate.support.RestTemplateUtils;
import io.github.requestlog.core.constant.Constants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicReference;


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


    /**
     * Perform request
     */
    protected RetryResult doExecute() {

        Preconditions.check(httpClient != null, "A restTemplate is null");
        Preconditions.check(super.validContext(), "retryContext is not valid for retry");


        HttpHeaders headers = RestTemplateUtils.convert2HttpHeaders(retryContext.buildRequestHeaders());
        headers.add(Constants.RETRY_HEADER, generateRetryHeaderValue());

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
