package com.github.requestlog.core.repository.impl;

import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.repository.IRequestLogRepository;
import javafx.util.Pair;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * In-memory implementation of a request log repository for testing scenarios.
 * This repository stores request logs in memory and is suitable for use in
 * test environments where a persistent storage solution is not required.
 *
 * Used for testing purposes; not thread-safe as no synchronization mechanisms,.
 */
public class InMemoryRequestLogRepository implements IRequestLogRepository {

    @Getter
    private final List<RequestLog> requestLogList = new ArrayList<>();

    @Getter
    private final List<RequestRetryJob> requestRetryJobList = new ArrayList<>();

    @Getter
    private final List<Pair<RequestLog, RequestRetryJob>> pairList = new ArrayList<>();

    @Override
    public void saveRequestLog(RequestLog requestLog) {
        requestLogList.add(requestLog);
    }

    @Override
    public void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestLogRetryJob) {
        requestLogList.add(requestLog);
        pairList.add(new Pair<>(requestLog, requestLogRetryJob));
    }

    /**
     * Get request log generated size.
     */
    public int getRequestLogSize() {
        return requestLogList.size();
    }

    /**
     * Get last generated {@link RequestLog}.
     */
    public RequestLog getLastRequestLog() {
        return CollectionUtils.lastElement(requestLogList);
    }

}