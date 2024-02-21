package io.github.requestlog.okhttp.interceptor;

import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.okhttp.context.request.OkHttpRequestContext;
import io.github.requestlog.okhttp.support.OkHttpUtils;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


/**
 * RequestLog interceptor for {@link OkHttpClient}
 */
@RequiredArgsConstructor
public class RequestLogOkHttpInterceptor implements Interceptor {


    private final AbstractRequestLogHandler requestLogHandler;


    @Override
    public Response intercept(Chain chain) throws IOException {

        // Skipped, no logging is specified or current request contains retry.
        if (LogContext.THREAD_LOCAL.get() == null || RetryContext.THREAD_LOCAL.get() != null) {
            return chain.proceed(chain.request());
        }

        Request request = chain.request();
        try {
            Response response = chain.proceed(request);
            response = OkHttpUtils.convertAsRepeatableRead(response);
            requestLogHandler.handle(new OkHttpRequestContext(LogContext.THREAD_LOCAL.get(), request, response));
            return response;
        } catch (Exception e) {
            requestLogHandler.handle(new OkHttpRequestContext(LogContext.THREAD_LOCAL.get(), request, e));
            throw e;
        }

    }
}
