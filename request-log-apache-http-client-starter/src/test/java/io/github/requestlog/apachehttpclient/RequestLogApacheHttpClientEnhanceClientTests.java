package io.github.requestlog.apachehttpclient;


import io.github.requestlog.apachehttpclient.support.ApacheHttpClientRequestLogEnhancer;
import io.github.requestlog.core.annotation.RequestLogEnhanced;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * Tests for enhance by properties.
 */
public class RequestLogApacheHttpClientEnhanceClientTests {


    @SpringBootTest(classes = TestApplication.class,
            properties = {
                    "debug=true",
                    // properties for enhance.
                    "request-log.apache-http-client.enhance-all=true"
            }
    )
    @Slf4j
    public static class EnhancedAllTests {

        @Resource
        private HttpClient httpClient;
        @Resource
        private HttpClient notEnhancedByAnnotationClient;
        @Resource
        private ApacheHttpClientRequestLogEnhancer enhancer;

        @Test
        public void testEnhancedAll() {
            assert enhancer.isEnhanced(httpClient);
            assert enhancer.isEnhanced(notEnhancedByAnnotationClient);
        }
    }


    @SpringBootTest(classes = TestApplication.class,
            properties = {
                    "debug=true"
            }
    )
    public static class NotEnhancedAll {

        @Resource
        private HttpClient httpClient;
        @Resource
        private HttpClient notEnhancedByAnnotationClient;
        @Resource
        private ApacheHttpClientRequestLogEnhancer enhancer;

        @Test
        public void testNotEnhancedAll() {
            assert enhancer.isEnhanced(httpClient);
            assert !enhancer.isEnhanced(notEnhancedByAnnotationClient);
        }
    }


    /**
     * Not enhanced by {@link RequestLogEnhanced} configuration.
     */
    @Configuration
    protected static class NotEnhancedHttpClientConfiguration {
        @Bean
        public HttpClient notEnhancedByAnnotationClient() {
            return HttpClients.createDefault();
        }
    }


}
