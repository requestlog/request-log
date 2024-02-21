package io.github.requestlog.core.handler.impl;

import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.core.repository.IRequestLogRepository;


/**
 * Default {@link AbstractRequestLogHandler}
 */
public class DefaultRequestLogHandler extends AbstractRequestLogHandler {

    public DefaultRequestLogHandler(IRequestLogRepository requestLogRepository) {
        super(requestLogRepository);
    }

}
