package io.github.requestlog.okhttp;


import io.github.requestlog.core.annotation.RequestLogEnhanced;
import io.github.requestlog.okhttp.support.OkHttpRequestLogEnhancer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Tests for enhance by properties.
 */
public class RequestLogOkHttpEnhanceClientTests {

    @SpringBootTest(classes = TestApplication.class,
            properties = {
                    "debug=true",
                    // properties for enhance.
                    "request-log.ok-http.enhance-all=true"
            }
    )
    @Slf4j
    public static class EnhanceAllTests {

        @Resource
        private OkHttpClient okHttpClient;
        @Resource
        private OkHttpClient notEnhancedByAnnotationClient;
        @Resource
        private OkHttpRequestLogEnhancer enhancer;

        @Test
        public void testEnhanceAll() {
            assert enhancer.isEnhanced(okHttpClient);
            assert enhancer.isEnhanced(notEnhancedByAnnotationClient);
        }


    }

    @SpringBootTest(classes = TestApplication.class,
            properties = {
                    "debug=true"
            }
    )
    @Slf4j
    public static class NotEnhanceAllTests {

        @Resource
        private OkHttpClient okHttpClient;
        @Resource
        private OkHttpClient notEnhancedByAnnotationClient;
        @Resource
        private OkHttpRequestLogEnhancer enhancer;

        @Test
        public void testNotEnhanceAll() {
            assert enhancer.isEnhanced(okHttpClient);
            assert !enhancer.isEnhanced(notEnhancedByAnnotationClient);
        }

    }


    /**
     * Not enhanced by {@link RequestLogEnhanced} configuration.
     */
    @Configuration
    protected static class NotEnhancedOkHttpClientConfiguration {
        @Bean
        public OkHttpClient notEnhancedByAnnotationClient() {
            return new OkHttpClient.Builder().build();
        }
    }

}
