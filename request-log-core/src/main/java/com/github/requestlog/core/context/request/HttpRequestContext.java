package com.github.requestlog.core.context.request;

import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.support.HttpUtils;

import java.util.List;
import java.util.Map;


public interface HttpRequestContext {


    HttpMethod getRequestMethod();

    String getRequestUrl();

    String getRequestPath();

    Map<String, List<String>> getRequestHeaders();

    default String getRequestContentType() {
        return HttpUtils.findContentType(getRequestHeaders());
    }

    String getRequestBody();


    Integer getResponseCode();

    Map<String, List<String>> getResponseHeaders();


    /**
     * May be called multiple times.
     * You should cache the return value.
     */
    String getResponseBody();

}
