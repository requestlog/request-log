package io.github.requestlog.core.model;

import io.github.requestlog.core.enums.RetryWaitStrategy;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class RequestRetryJob {

    private RequestLog requestLog;

    /**
     * Retry interval calculation strategy.
     */
    private RetryWaitStrategy retryWaitStrategy;

    /**
     * Retry interval in seconds.
     */
    private Integer retryInterval;

    /**
     * Last execution time.
     */
    private Long lastExecuteTimeMillis;

    /**
     * Next scheduled execution time.
     */
    private Long nextExecuteTimeMillis;

    /**
     * Number of executions already performed.
     *
     * Contains first execution count when RequestLog is created.
     * Starts from 1.
     */
    private Integer executeCount;

    // TODO: 2024/2/21 expectMaxRetryCount ?

}
