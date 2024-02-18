package com.github.requestlog.core.model;

import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.enums.RequestContextType;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * Http request context pojo.
 */
@Data
public class HttpRequestContext {

    private HttpMethod httpMethod;
    private RequestContextType requestContextType;

    private String requestUrl;
    private String requestPath;
    private Map<String, List<String>> requestHeaders;
    private String requestBody;

    // When an exception occurs during the request, the response-related fields will be null.
    private Integer responseCode;
    private Map<String, List<String>> responseHeaders;
    private String responseBody;

}
