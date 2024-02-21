package io.github.requestlog.resttemplate.autoconfigure;

import io.github.requestlog.core.annotation.RequestLogEnhanced;
import io.github.requestlog.resttemplate.support.RestTemplateRequestLogEnhancer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.client.RestTemplate;


/**
 * BeanPostProcessor to enhance {@link RestTemplate} based on the presence of the {@link RequestLogEnhanced} annotation.
 */
@Slf4j
@RequiredArgsConstructor
public class RequestLogRestTemplateBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private final RestTemplateRequestLogEnhancer enhancer;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RestTemplate && applicationContext.findAnnotationOnBean(beanName, RequestLogEnhanced.class) != null) {
            return enhancer.enhance((RestTemplate) bean);
        }
        return bean;
    }

}
