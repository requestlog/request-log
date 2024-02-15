package com.github.requestlog.core.repository;

import com.github.requestlog.core.enums.RetryWaitStrategy;
import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.model.RequestRryLog;


public interface IRequestLogRepository {

    void saveRequestLog(RequestLog requestLog);

    void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestRetryJob);

    default void saveRequestRetryJob(RequestRetryJob requestRetryJob) {
    }


    default void saveRequestRetryLog(RequestRryLog requestRryLog) {
    }


    /**
     * Generate a {@link RequestRetryJob} from given {@link RequestLog}
     */
    default RequestRetryJob generateNewRetryJob(RequestLog requestLog) {
        return generateNewRetryJob(requestLog, RetryWaitStrategy.FIXED, 60);
    }

    /**
     * Generate a {@link RequestRetryJob} from given {@link RequestLog}
     */
    default RequestRetryJob generateNewRetryJob(RequestLog requestLog, RetryWaitStrategy retryWaitStrategy, int retryInterval) {

        RequestRetryJob retryJob = new RequestRetryJob();
        retryJob.setRequestLog(requestLog);
        retryJob.setRetryWaitStrategy(retryWaitStrategy);
        retryJob.setRetryInterval(retryInterval);
        retryJob.setLastExecuteTimeMillis(0L);
        retryJob.setExecuteCount(1); // TODO: 2024/2/14 new generated starts from 0 or 1
        // TODO: 2024/2/14 retry 60s later or now
        retryJob.setNextExecuteTimeMillis(retryWaitStrategy.nextExecuteTime(System.currentTimeMillis(), retryJob.getExecuteCount(), retryInterval));

        return retryJob;
    }

}
