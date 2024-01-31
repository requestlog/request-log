package com.github.requestlog.core.repository;

import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;


public interface IRequestLogRepository {

    void saveRequestLog(RequestLog requestLog);

    void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestLogRetryJob);

}
