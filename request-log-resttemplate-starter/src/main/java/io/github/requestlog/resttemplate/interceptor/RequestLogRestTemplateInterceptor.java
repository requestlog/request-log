package io.github.requestlog.resttemplate.interceptor;

import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.resttemplate.context.request.RestTemplateRequestContext;
import io.github.requestlog.resttemplate.support.RestTemplateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;


/**
 * RequestLog interceptor for {@link org.springframework.web.client.RestTemplate}
 */
@RequiredArgsConstructor
public class RequestLogRestTemplateInterceptor implements ClientHttpRequestInterceptor {


    private final AbstractRequestLogHandler requestLogHandler;


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        // Skipped, no logging is specified or current request contains retry.
        if (LogContext.THREAD_LOCAL.get() == null || RetryContext.THREAD_LOCAL.get() != null) {
            return execution.execute(request, body);
        }

        try {
            // Perform request and convert response as body repeatable read response.
            ClientHttpResponse clientHttpResponse = RestTemplateUtils.convert2RepeatableBodyResponse(execution.execute(request, body));
            requestLogHandler.handle(new RestTemplateRequestContext(LogContext.THREAD_LOCAL.get(), request, body, clientHttpResponse));
            return clientHttpResponse;
        } catch (Exception e) {
            requestLogHandler.handle(new RestTemplateRequestContext(LogContext.THREAD_LOCAL.get(), request, body, e));
            throw e;
        }

    }

}
