package com.github.requestlog.core.context.request;

import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.support.CollectionUtils;

import java.util.List;
import java.util.Map;


public interface HttpRequestContext {


    HttpMethod getRequestMethod();

    String getRequestUrl();

    String getRequestPath();

    Map<String, List<String>> getRequestHeaders();

    // TODO: 2024/1/31
    default String getRequestContentType() {
        return CollectionUtils.firstElement(getRequestHeaders().get("content-type"));
    }

    // TODO: 2024/1/31
    String getRequestParams();

    String getRequestBody();


    Integer getResponseCode();

    Map<String, List<String>> getResponseHeaders();


    /**
     * May be called multiple times.
     * You should cache the return value.
     */
    String getResponseBody();

}
