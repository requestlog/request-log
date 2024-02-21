package io.github.requestlog.resttemplate.support;

import io.github.requestlog.resttemplate.interceptor.RequestLogRestTemplateInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;


/**
 * Enhances a {@link RestTemplate} with RequestLog capabilities.
 */
@Slf4j
@RequiredArgsConstructor
public class RestTemplateRequestLogEnhancer {


    private final RequestLogRestTemplateInterceptor interceptor;


    /**
     * Enhances the provided {@link RestTemplate} with RequestLog capabilities.
     */
    public RestTemplate enhance(RestTemplate restTemplate) {
        RestTemplateUtils.addInterceptor(restTemplate, interceptor);
        return restTemplate;
    }

}
