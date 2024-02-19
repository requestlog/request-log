package com.github.requestlog.core.support;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


/**
 * Utility class for HTTP-related operations.
 */
public class HttpUtils {


    /**
     * Find the 'Content-Type' value from the given HTTP headers map.
     */
    public static String findContentType(Map<String, List<String>> headerMap) {
        if (CollectionUtils.isEmpty(headerMap)) {
            return null;
        }
        for (String key : headerMap.keySet()) {
            if ("content-type".equalsIgnoreCase(key)) {
                List<String> values = headerMap.get(key);
                if (!CollectionUtils.isEmpty(values)) {
                    return CollectionUtils.firstElement(values);
                }
            }
        }
        return null;
    }

}
