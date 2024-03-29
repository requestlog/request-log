package io.github.requestlog.servlet.autoconfigure;


import io.github.requestlog.core.autoconfigure.RequestLogConfiguration;
import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.servlet.aop.RequestLogServletAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * RequestLog configuration for Servlet.
 */
@Configuration(proxyBeanMethods = false)
@Import(RequestLogConfiguration.class)
public class RequestLogServletAutoConfiguration {


    /**
     * Configuration class for creating {@link RequestLogServletAdvice}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RequestLogServletAdvice.class)
    protected static class AopConfiguration {

        @Bean
        public RequestLogServletAdvice requestLogServletAdvice(@Autowired AbstractRequestLogHandler requestLogHandler) {
            return new RequestLogServletAdvice(requestLogHandler);
        }

    }

}
