package com.github.requestlog.okhttp.support;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


/**
 * Utility class for working with {@link OkHttpClient}.
 */
public class OkHttpUtils {


    /**
     * Converts the request body of the provided {@link Request} to a string.
     */
    public static String requestBody2String(Request request) {
        if (request == null || request.body() == null) {
            return null;
        }
        try {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (Exception ignored) {
            return null;
        }
    }


    /**
     * Converts the response body of the provided {@link Response} to a string.
     */
    public static String responseBody2String(Response response) {
        if (response == null || response.body() == null) {
            return null;
        }
        try {
            return response.body().string();
        } catch (Exception ignored) {
        }
        return null;
    }


    /**
     * Checks if the body of the given {@link Response} is repeatable.
     */
    public static boolean isResponseRepeatable(Response response) {
        return response != null && response.body() instanceof RepeatableResponseBodyWrapper;
    }


    /**
     * Convert a {@link Response} to a new response with a repeatable body.
     * If the body is already repeatable, return the original response without re-conversion.
     */
    public static Response convertAsRepeatableRead(Response response) {
        if (response == null) {
            return null;
        }
        if (response.body() instanceof RepeatableResponseBodyWrapper) {
            return response;
        }
        return response.newBuilder().body(new RepeatableResponseBodyWrapper(response.body())).build();
    }


    /**
     * Convert a Map of headers to OkHttp3 {@link Headers}.
     */
    public static Headers convertToHeaders(Map<String, List<String>> headerMap) {
        if (CollectionUtils.isEmpty(headerMap)) {
            return new Headers.Builder().build();
        }

        Headers.Builder headersBuilder = new Headers.Builder();

        for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            if (key != null && values != null) {
                for (String value : values) {
                    headersBuilder.add(key, value);
                }
            }
        }

        return headersBuilder.build();
    }


}
