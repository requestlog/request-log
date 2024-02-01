package com.github.requestlog.resttemplate.autoconfigure;

import com.github.requestlog.core.config.RequestLogConfiguration;
import com.github.requestlog.core.handler.AbstractRequestLogHandler;
import com.github.requestlog.resttemplate.interceptor.RequestLogRestTemplateInterceptor;
import com.github.requestlog.resttemplate.support.RestTemplateUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


/**
 * RequestLog configuration for {@link RestTemplate}
 */
@Configuration(proxyBeanMethods = false)
@Import(RequestLogConfiguration.class)
public class RequestLogRestTemplateAutoConfiguration {


    /**
     * Configuration class for creating {@link RequestLogRestTemplateInterceptor}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RequestLogRestTemplateInterceptor.class)
    protected static class InterceptorConfiguration {

        @Bean
        public RequestLogRestTemplateInterceptor requestLogRestTemplateInterceptor(@Autowired AbstractRequestLogHandler requestLogHandler) {
            return new RequestLogRestTemplateInterceptor(requestLogHandler);
        }

    }

    /**
     * Configuration class for registering the {@link RequestLogRestTemplateInterceptor}.
     * This configuration is activated when the property 'request-log.rest-template.enhance-all' is set to 'true' (or not set, considering the default match).
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean({RestTemplate.class, RequestLogRestTemplateInterceptor.class})
    @ConditionalOnProperty(value = "request-log.rest-template.enhance-all", havingValue = "true", matchIfMissing = true)
    protected static class RegisterInterceptorConfiguration {

        /**
         * Creates and registers a {@link SmartInitializingSingleton} bean that initializes the {@link RequestLogRestTemplateInterceptor}
         * for all {@link RestTemplate} instances available in the application context.
         */
        @Bean
        public SmartInitializingSingleton registerRequestLogRestTemplateInterceptorInitializer(@Autowired RequestLogRestTemplateInterceptor interceptor,
                                                                                               @Autowired Map<String, RestTemplate> restTemplateMap) {
            return () -> restTemplateMap.values().forEach(restTemplate -> RestTemplateUtils.addInterceptor(restTemplate, interceptor));
        }

    }

    // TODO: 2024/1/28 manually register

}
