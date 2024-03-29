package io.github.requestlog.okhttp.context.retry;


import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.context.retry.RetryClient;
import io.github.requestlog.core.context.retry.RetryResult;
import io.github.requestlog.core.enums.HttpMethod;
import io.github.requestlog.core.enums.RetryClientType;
import io.github.requestlog.core.support.Preconditions;
import io.github.requestlog.okhttp.context.request.OkHttpRequestContext;
import io.github.requestlog.okhttp.support.OkHttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * RetryClient for {@link OkHttpClient}
 */
public class OkHttpRetryClient extends RetryClient<OkHttpClient> {

    static {
        RetryClient.NEW_INSTANCE_MAP.put(OkHttpRetryClient.class, OkHttpRetryClient::new);
    }

    public OkHttpRetryClient(RetryContext retryContext) {
        super(retryContext);
    }

    @Override
    protected RetryResult doExecute() {

        Preconditions.check(httpClient != null, "A okHttpClient is null");
        Preconditions.check(super.validContext(), "retryContext is not valid for retry");

        HttpMethod method = retryContext.getRequestLog().getHttpMethod();

        Request.Builder builder = new Request.Builder()
                .url(retryContext.buildRequestUrl())
                .headers(OkHttpUtils.convertToHeaders(retryContext.buildRequestHeaders()))
                .method(method.name(), !method.supportsRequestBody() ? null : RequestBody.create(null, retryContext.getRequestLog().getRequestBody()));
        Request request = builder.build();

        try (Response response = OkHttpUtils.convertAsRepeatableRead(httpClient.newCall(request).execute())) {
            return new RetryResult(RetryClientType.OK_HTTP, beforeDoExecuteTimeMillis, retryContext, new OkHttpRequestContext(null, request, response).buildHttpRequestContext());
        } catch (Exception e) {
            return new RetryResult(RetryClientType.OK_HTTP, beforeDoExecuteTimeMillis, retryContext, new OkHttpRequestContext(null, request, e).buildHttpRequestContext());
        }
    }

}

