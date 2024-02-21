package io.github.requestlog.okhttp;


import io.github.requestlog.core.annotation.RequestLogEnhanced;
import io.github.requestlog.core.repository.IRequestLogRepository;
import io.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class OkHttpTestConfiguration {

    @Bean
    public IRequestLogRepository requestLogRepository() {
        return new InMemoryRequestLogRepository();
    }

    @RequestLogEnhanced
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

}
