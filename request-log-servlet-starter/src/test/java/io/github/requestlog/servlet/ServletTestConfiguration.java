package io.github.requestlog.servlet;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.requestlog.core.repository.IRequestLogRepository;
import io.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import io.github.requestlog.test.support.ObjectToUrlEncodedConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration(proxyBeanMethods = false)
public class ServletTestConfiguration {

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(new ObjectMapper()));
        return restTemplate;
    }

    @Bean
    public IRequestLogRepository requestLogRepository() {
        return new InMemoryRequestLogRepository();
    }

}
