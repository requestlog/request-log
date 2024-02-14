package com.github.requestlog.core.model;

import com.github.requestlog.core.enums.RetryClientType;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;


@Data
@ToString
public class RequestRryLog {

    private RequestLog requestLog;
    private RequestRetryJob requestRetryJob;

    private RetryClientType retryClientType;
    private Boolean succeed;

    private Integer executeCount;
    private Long executeTimeMillis;

    // Exception
    private Exception exception;

    // request
    private String requestUrl;
    private Map<String, List<String>> requestHeaders;
    private String requestBody;

    // response
    private Integer responseCode;
    private Map<String, List<String>> responseHeaders;
    private String responseBody;

}
