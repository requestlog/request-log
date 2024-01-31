package com.github.requestlog.core.config;

import com.github.requestlog.core.handler.impl.DefaultRequestLogHandler;
import com.github.requestlog.core.handler.AbstractRequestLogHandler;
import com.github.requestlog.core.repository.IRequestLogRepository;
import com.github.requestlog.core.repository.impl.Slf4jRequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Core configuration for RequestLog.
 */
@Configuration(proxyBeanMethods = false)
public class RequestLogConfiguration {


    /**
     * Default {@link IRequestLogRepository}.
     */
    @Configuration
    @ConditionalOnMissingBean(IRequestLogRepository.class)
    protected static class RepositoryConfiguration {

        @Bean
        public IRequestLogRepository requestLogRepository() {
            return new Slf4jRequestLogRepository();
        }

    }

    /**
     * Default {@link AbstractRequestLogHandler}.
     */
    @Configuration
    @ConditionalOnMissingBean(AbstractRequestLogHandler.class)
    public static class HandlerConfiguration {

        @Bean
        public AbstractRequestLogHandler requestLogHandler(@Autowired IRequestLogRepository requestLogRepository) {
            return new DefaultRequestLogHandler(requestLogRepository);
        }

    }

}

