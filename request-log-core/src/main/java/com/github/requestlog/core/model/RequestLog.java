package com.github.requestlog.core.model;


import com.github.requestlog.core.enums.RequestContextType;
import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.enums.RequestLogErrorType;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class RequestLog {

    // context-type、http-client-type
    private RequestContextType contextType;

    private RequestLogErrorType logErrorType;

    // Exception
    private Exception exception;

    // http request context
    private HttpMethod httpMethod;
    private String requestUrl;
    private String requestPath;
    private Map<String, List<String>> requestHeaders;
    // TODO: 2024/1/31 requestParams?
    private String requestBody;

    // http response context
    private Integer responseCode;
    private Map<String, List<String>> responseHeaders;
    private String responseBody;

}