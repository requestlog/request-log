package io.github.requestlog.core.handler;

import io.github.requestlog.core.context.request.BaseRequestContext;
import io.github.requestlog.core.repository.IRequestLogRepository;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class AbstractRequestLogHandler {

    protected final IRequestLogRepository requestLogRepository;


    // TODO: 2024/1/31 extension methods behaviors
    public void handle(BaseRequestContext requestContext) {

        if (requestContext == null) {
            return;
        }

        if (!requestContext.logRequest()) {
            return;
        }

        if (!requestContext.retryRequest()) {
            requestLogRepository.saveRequestLog(requestContext.buildRequestLog());
        } else {
            requestLogRepository.saveRequestLogAndRetryJob(requestContext.buildRequestLog(), requestContext.buildRequestRetryJob());
        }

    }

}
