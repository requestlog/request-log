package com.github.requestlog.core.repository.impl;

import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.model.RequestRryLog;
import com.github.requestlog.core.repository.IRequestLogRepository;
import lombok.extern.slf4j.Slf4j;


/**
 * SLF4J implementation of a request log repository.
 */
@Slf4j
public class Slf4jRequestLogRepository implements IRequestLogRepository {

    @Override
    public void saveRequestLog(RequestLog requestLog) {
        log.debug("save requestLog: {}", requestLog);
    }

    @Override
    public void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestRetryJob) {
        log.debug("save requestLog: {}, requestRetryJob: {}", requestLog, requestRetryJob);
    }

    @Override
    public void saveRequestRetryJob(RequestRetryJob requestRetryJob) {
        log.debug("save requestRetryJob: {}", requestRetryJob);
    }

    @Override
    public void saveRequestRetryLog(RequestRryLog requestRetryLog) {
        log.debug("save requestRetryLog: {}", requestRetryLog);
    }

}
