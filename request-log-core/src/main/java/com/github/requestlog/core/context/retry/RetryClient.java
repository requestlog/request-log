package com.github.requestlog.core.context.retry;

import com.github.requestlog.core.context.RetryContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public abstract class RetryClient {

    protected final RetryContext retryContext;


    public static final Map<Class<? extends RetryClient>, Function<RetryContext, RetryClient>> NEW_INSTANCE_MAP = new HashMap<>();


    // TODO: 2024/2/14 checks if the retryContext executable
    protected boolean validContext() {
        // TODO: 2024/2/11
        return false;
    }


}
