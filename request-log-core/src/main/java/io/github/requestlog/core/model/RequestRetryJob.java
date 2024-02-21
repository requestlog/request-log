package io.github.requestlog.core.model;

import io.github.requestlog.core.enums.RetryWaitStrategy;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class RequestRetryJob {

    private RequestLog requestLog;

    // Retry interval calculation strategy
    private RetryWaitStrategy retryWaitStrategy;

    // TODO: 2024/2/14 change to millis?
    // Retry interval in seconds
    private Integer retryInterval;

    // Last execution time
    private Long lastExecuteTimeMillis;

    // Next scheduled execution time
    private Long nextExecuteTimeMillis;

    // TODO: 2024/2/14 contains first execute time?
    // Number of retries already performed (contains first execute count)
    private Integer executeCount;

}
