package com.github.requestlog.core.model;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class RequestRetryJob {

    private RequestLog requestLog;

    // TODO: 2024/1/31 retry fields
}
