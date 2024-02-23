package io.github.requestlog.okhttp.support;


import io.github.requestlog.okhttp.interceptor.RequestLogOkHttpInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;


/**
 * Enhances an {@link OkHttpClient} with RequestLog capabilities.
 */
@Slf4j
@RequiredArgsConstructor
public class OkHttpRequestLogEnhancer {


    private final RequestLogOkHttpInterceptor interceptor;


    /**
     * Enhances the provided {@link OkHttpClient} with RequestLog capabilities.
     *
     * If the Interceptor already exists, returns the original object.
     * If the Interceptor does not exist, creates a enhanced object.
     */
    public OkHttpClient enhance(OkHttpClient client) {
        if (isEnhanced(client)) {
            return client;
        }
        return client.newBuilder().addInterceptor(interceptor).build();
    }

    /**
     * Checks if the {@link OkHttpClient} is already enhanced.
     */
    public boolean isEnhanced(OkHttpClient client) {
        for (Interceptor clientInterceptor : client.interceptors()) {
            if (clientInterceptor.getClass().equals(RequestLogOkHttpInterceptor.class)) {
                return true;
            }
        }
        return false;
    }

}
