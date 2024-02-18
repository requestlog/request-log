package com.github.requestlog.core.enums;


import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpMethod {

    // TODO: 2024/1/31 more headers?
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;


    private static final Map<String, HttpMethod> UPPERCASE_NAME_MAP = Arrays.stream(HttpMethod.values())
            .collect(Collectors.toMap(e -> e.name().toUpperCase(), Function.identity()));


    /**
     * Returns {@link HttpMethod} by http method name.
     */
    public static HttpMethod of(String httpMethodName) {
        if (!StringUtils.hasText(httpMethodName)) {
            return null;
        }
        return UPPERCASE_NAME_MAP.get(httpMethodName.toUpperCase());
    }


    /**
     * Check if this HTTP method supports a request body.
     */
    public boolean supportsRequestBody() {
        return this == POST || this == PUT || this == DELETE || this == PATCH;
    }


}
