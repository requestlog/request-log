package io.github.requestlog.okhttp.autoconfigure;

import io.github.requestlog.core.autoconfigure.RequestLogConfiguration;
import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.okhttp.interceptor.RequestLogOkHttpInterceptor;
import io.github.requestlog.okhttp.support.OkHttpRequestLogEnhancer;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * RequestLog configuration for {@link OkHttpClient}
 */
@Configuration(proxyBeanMethods = false)
@Import(RequestLogConfiguration.class)
public class RequestLogOkHttpAutoConfiguration {


    /**
     * Configuration class for creating {@link RequestLogOkHttpInterceptor}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RequestLogOkHttpInterceptor.class)
    protected static class RequestLogOkHttpInterceptorConfiguration {

        @Bean
        public RequestLogOkHttpInterceptor requestLogOkHttpInterceptor(@Autowired AbstractRequestLogHandler handler) {
            return new RequestLogOkHttpInterceptor(handler);
        }

    }

    /**
     * Configuration class for creating {@link OkHttpRequestLogEnhancer}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(OkHttpRequestLogEnhancer.class)
    protected static class OkHttpRequestLogEnhancerConfiguration {

        @Bean
        public OkHttpRequestLogEnhancer okHttpRequestLogEnhancer(@Autowired RequestLogOkHttpInterceptor interceptor) {
            return new OkHttpRequestLogEnhancer(interceptor);
        }

    }


    /**
     * Configuration class for creating {@link RequestLogOkHttpBeanPostProcessor}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RequestLogOkHttpBeanPostProcessor.class)
    protected static class RequestLogOkHttpBeanPostProcessorConfiguration {

        @Bean
        public RequestLogOkHttpBeanPostProcessor requestLogOkHttpBeanPostProcessor(@Autowired OkHttpRequestLogEnhancer enhancer) {
            return new RequestLogOkHttpBeanPostProcessor(enhancer);
        }

    }

}
