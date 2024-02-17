package com.github.requestlog.okhttp.support;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;


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


}
