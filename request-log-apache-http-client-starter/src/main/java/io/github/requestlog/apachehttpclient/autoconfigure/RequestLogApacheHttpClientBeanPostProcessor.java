package io.github.requestlog.apachehttpclient.autoconfigure;

import io.github.requestlog.apachehttpclient.support.ApacheHttpClientRequestLogEnhancer;
import io.github.requestlog.core.annotation.RequestLogEnhanced;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * BeanPostProcessor to enhance {@link HttpClient} based on the presence of the {@link RequestLogEnhanced} annotation.
 */
@Slf4j
@RequiredArgsConstructor
public class RequestLogApacheHttpClientBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private final ApacheHttpClientRequestLogEnhancer enhancer;

    private ApplicationContext applicationContext;

    @Value("${request-log.apache-http-client.enhance-all:#{null}}")
    private String enhanceAll;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HttpClient) {
            if ("true".equals(enhanceAll) || applicationContext.findAnnotationOnBean(beanName, RequestLogEnhanced.class) != null) {
                return enhancer.enhance((HttpClient) bean);
            }
        }
        return bean;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }



}