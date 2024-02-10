package com.github.requestlog.apachehttpclient.autoconfigure;


import com.github.requestlog.apachehttpclient.support.ApacheHttpClientRequestLogEnhancer;
import com.github.requestlog.core.config.RequestLogConfiguration;
import com.github.requestlog.core.handler.AbstractRequestLogHandler;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * RequestLog configuration for {@link HttpClient}
 */
@Configuration(proxyBeanMethods = false)
@Import(RequestLogConfiguration.class)
public class RequestLogApacheHttpClientAutoConfiguration {


    /**
     * Configuration class for creating {@link ApacheHttpClientRequestLogEnhancer}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(ApacheHttpClientRequestLogEnhancer.class)
    protected static class ApacheHttpClientRequestLogEnhancerConfiguration {

        @Bean
        public ApacheHttpClientRequestLogEnhancer apacheHttpClientRequestLogEnhancer(@Autowired AbstractRequestLogHandler requestLogHandler) {
            return new ApacheHttpClientRequestLogEnhancer(requestLogHandler);
        }

    }


}
