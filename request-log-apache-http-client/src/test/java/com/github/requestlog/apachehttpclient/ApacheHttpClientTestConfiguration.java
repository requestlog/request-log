package com.github.requestlog.apachehttpclient;


import com.github.requestlog.apachehttpclient.support.ApacheHttpClientRequestLogEnhancer;
import com.github.requestlog.core.repository.IRequestLogRepository;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApacheHttpClientTestConfiguration {

    @Bean
    public IRequestLogRepository requestLogRepository() {
        return new InMemoryRequestLogRepository();
    }


    @Bean
    public HttpClient httpClient(@Autowired ApacheHttpClientRequestLogEnhancer requestLogEnhancer) {
        HttpClient httpClient = HttpClients.createDefault();
        return requestLogEnhancer.enhance(httpClient);
    }

}
