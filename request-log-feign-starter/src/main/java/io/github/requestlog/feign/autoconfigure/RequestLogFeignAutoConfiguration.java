package io.github.requestlog.feign.autoconfigure;


import io.github.requestlog.core.autoconfigure.RequestLogConfiguration;
import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.feign.aop.RequestLogFeignAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * RequestLog configuration for Feign.
 */
@Configuration(proxyBeanMethods = false)
@Import(RequestLogConfiguration.class)
public class RequestLogFeignAutoConfiguration {


    /**
     * Configuration class for creating {@link RequestLogFeignAdvice}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RequestLogFeignAdvice.class)
    protected static class AopConfiguration {

        @Bean
        public RequestLogFeignAdvice requestLogFeignAdvice(@Autowired AbstractRequestLogHandler requestLogHandler) {
            return new RequestLogFeignAdvice(requestLogHandler);
        }

    }

}
