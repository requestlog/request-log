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

    default String getRequestContentType() {
        Map<String, List<String>> headers = getRequestHeaders();
        if (CollectionUtils.isEmpty(headers)) {
            return null;
        }
        for (String key : headers.keySet()) {
            if ("content-type".equalsIgnoreCase(key)) {
                return CollectionUtils.firstElement(getRequestHeaders().get(key));
            }
        }
        return null;
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
