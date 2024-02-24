package io.github.requestlog.core.repository;

import io.github.requestlog.core.enums.RetryWaitStrategy;
import io.github.requestlog.core.model.RequestLog;
import io.github.requestlog.core.model.RequestRetryJob;
import io.github.requestlog.core.model.RequestRryLog;


public interface IRequestLogRepository {


    /**
     * Save {@link RequestLog}
     */
    void saveRequestLog(RequestLog requestLog);

    /**
     * Save {@link RequestRetryJob}
     */
    void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestRetryJob);


    /**
     * Save {@link RequestRetryJob}
     *
     * Typically saved manually after {@link #generateNewRetryJob}.
     */
    default void saveRequestRetryJob(RequestRetryJob requestRetryJob) {
    }

    /**
     * Save {@link RequestRryLog}
     */
    default void saveRequestRetryLog(RequestRryLog requestRryLog) {
    }


    /**
     * Generate a {@link RequestRetryJob} from given {@link RequestLog}
     */
    default RequestRetryJob generateNewRetryJob(RequestLog requestLog) {
        return generateNewRetryJob(requestLog, RetryWaitStrategy.FIXED, 60, 3);
    }

    /**
     * Generate a {@link RequestRetryJob} from given {@link RequestLog}
     */
    default RequestRetryJob generateNewRetryJob(RequestLog requestLog, RetryWaitStrategy retryWaitStrategy, int retryInterval, int maxExecuteCount) {

        RequestRetryJob retryJob = new RequestRetryJob();
        retryJob.setRequestLog(requestLog);
        retryJob.setRetryWaitStrategy(retryWaitStrategy);
        retryJob.setRetryInterval(retryInterval);
        retryJob.setLastExecuteTimeMillis(0L);
        retryJob.setExecuteCount(1); // 1 is for RequestLog first execution count.
        retryJob.setNextExecuteTimeMillis(System.currentTimeMillis()); // instant retry.
        retryJob.setMaxExecuteCount(maxExecuteCount);

        return retryJob;
    }

}
