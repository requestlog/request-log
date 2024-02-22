package io.github.requestlog.apachehttpclient.support;

import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;


/**
 * Enhances an {@link HttpClient} with RequestLog capabilities.
 */
@Slf4j
@RequiredArgsConstructor
public class ApacheHttpClientRequestLogEnhancer {


    private final AbstractRequestLogHandler requestLogHandler;


    /**
     * Enhances the provided {@link HttpClient} with RequestLog capabilities.
     */
    public HttpClient enhance(HttpClient httpClient) {
        if (isEnhanced(httpClient)) {
            return httpClient;
        }
        return new HttpClientRequestLogDecorator(requestLogHandler, httpClient);
    }


    /**
     * Checks if the {@link HttpClient} is already enhanced.
     *
     * Does not support decorators with multiple layers.
     */
    public boolean isEnhanced(HttpClient httpClient) {
        return httpClient instanceof HttpClientRequestLogDecorator;
    }

}
