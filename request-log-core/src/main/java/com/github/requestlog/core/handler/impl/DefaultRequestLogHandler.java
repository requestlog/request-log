package com.github.requestlog.core.handler.impl;

import com.github.requestlog.core.handler.AbstractRequestLogHandler;
import com.github.requestlog.core.repository.IRequestLogRepository;


/**
 * Default {@link AbstractRequestLogHandler}
 */
public class DefaultRequestLogHandler extends AbstractRequestLogHandler {

    public DefaultRequestLogHandler(IRequestLogRepository requestLogRepository) {
        super(requestLogRepository);
    }

}
