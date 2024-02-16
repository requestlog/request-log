package com.github.requestlog.apachehttpclient;


import com.github.requestlog.core.annotation.RequestLogEnhanced;
import com.github.requestlog.core.repository.IRequestLogRepository;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApacheHttpClientTestConfiguration {

    @Bean
    public IRequestLogRepository requestLogRepository() {
        return new InMemoryRequestLogRepository();
    }


    @RequestLogEnhanced
    @Bean
    public HttpClient httpClient() {
        return HttpClients.createDefault();
    }

}
