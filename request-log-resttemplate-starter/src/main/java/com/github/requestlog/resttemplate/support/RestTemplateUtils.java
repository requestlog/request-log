package com.github.requestlog.resttemplate.support;

import com.github.requestlog.resttemplate.support.spring.BodyCacheClientHttpResponseWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


/**
 * Utility class for working with RestTemplate.
 */
public class RestTemplateUtils {

    /**
     * Checks if the body of the given {@link ClientHttpResponse} is repeatable.
     */
    public static boolean isRepeatableBody(ClientHttpResponse response) {
        if (response == null) {
            return false;
        }
        return "org.springframework.http.client.BufferingClientHttpResponseWrapper".equals(response.getClass().getName())
                || BodyCacheClientHttpResponseWrapper.class == response.getClass();
    }


    /**
     * Converts a {@link ClientHttpResponse} to a response with a repeatable body.
     * If the body of the original response is already repeatable, no enhancement will be performed.
     */
    public static ClientHttpResponse convert2RepeatableBodyResponse(ClientHttpResponse response) {
        if (response == null || isRepeatableBody(response)) {
            return response;
        }
        return new BodyCacheClientHttpResponseWrapper(response);
    }


    /**
     * Adds a custom {@link ClientHttpRequestInterceptor} to the provided {@link RestTemplate} instance
     * if an interceptor of the same class does not already exist in its list of interceptors.
     *
     * @param restTemplate                 The {@link RestTemplate} to which the interceptor should be added.
     * @param clientHttpRequestInterceptor The custom {@link ClientHttpRequestInterceptor} to be added.
     */
    public static void addInterceptor(RestTemplate restTemplate, ClientHttpRequestInterceptor clientHttpRequestInterceptor) {
        // Check if an interceptor of the same class already exists
        for (ClientHttpRequestInterceptor interceptor : restTemplate.getInterceptors()) {
            if (interceptor.getClass().equals(clientHttpRequestInterceptor.getClass())) {
                // If exists, do not add it again
                return;
            }
        }
        restTemplate.getInterceptors().add(clientHttpRequestInterceptor);
    }


    /**
     * Converts a Map of headers to HttpHeaders.
     */
    public static HttpHeaders convert2HttpHeaders(Map<String, List<String>> headersMap) {
        HttpHeaders headers = new HttpHeaders();
        if (!CollectionUtils.isEmpty(headersMap)) {
            headersMap.forEach(headers::addAll);
        }
        return headers;
    }

}
