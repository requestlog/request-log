package io.github.requestlog.okhttp.autoconfigure;

import io.github.requestlog.core.annotation.RequestLogEnhanced;
import io.github.requestlog.okhttp.support.OkHttpRequestLogEnhancer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * BeanPostProcessor to enhance {@link OkHttpClient} based on the presence of the {@link RequestLogEnhanced} annotation.
 */
@Slf4j
@RequiredArgsConstructor
public class RequestLogOkHttpBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private final OkHttpRequestLogEnhancer enhancer;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof OkHttpClient && applicationContext.findAnnotationOnBean(beanName, RequestLogEnhanced.class) != null) {
            return enhancer.enhance((OkHttpClient) bean);
        }
        return bean;
    }

}
