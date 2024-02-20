package com.github.requestlog.core.context;

import com.github.requestlog.core.context.retry.RetryClient;
import com.github.requestlog.core.model.HttpRequestContext;
import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.repository.IRequestLogRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Slf4j
@Getter
public class RetryContext {

    /**
     * ThreadLocal for {@link RetryContext}
     */
    // TODO: 2024/2/14 retry loop, usually outbound retry request meets a inbound log
    public static final ThreadLocal<RetryContext> THREAD_LOCAL = new InheritableThreadLocal<>();


    @Getter
    private final RequestLog requestLog;
    @Getter
    private final RequestRetryJob requestRetryJob;

    private RetryContext(RequestLog requestLog, RequestRetryJob retryJob) {
        assert requestLog != null;
        this.requestLog = requestLog;
        this.requestRetryJob = retryJob;
    }

    /**
     * Create by {@link RequestLog}
     * Single manual retry. If a {@link RequestRetryJob} is needed, it can be created using {@link IRequestLogRepository#generateNewRetryJob}.
     */
    public static RetryContext create(RequestLog requestLog) {
        return create(requestLog, null);
    }

    /**
     * Create by {@link RequestLog} and {@link RequestRetryJob}
     */
    public static RetryContext create(RequestLog requestLog, RequestRetryJob retryJob) {
        return new RetryContext(requestLog, retryJob);
    }


    public <C, T extends RetryClient<C>> T with(Class<T> retryClientClazz) {
        return with(retryClientClazz, null);
    }


    @SuppressWarnings("unchecked")
    public <C, T extends RetryClient<C>> T with(Class<T> retryClientClazz, @Nullable C httpClient) {

        T retryClient = null;

        Function<RetryContext, RetryClient<?>> function = RetryClient.NEW_INSTANCE_MAP.get(retryClientClazz);
        if (function != null) {
            retryClient = (T) function.apply(this);
        }

        // RetryClient 子类可能没有加载，没有将 Function 注册进去，这里尝试使用反射
        try {
            retryClient = retryClientClazz.getConstructor(RetryContext.class).newInstance(this);
        } catch (Exception ignored) {
        }

        if (retryClient == null) {
            throw new IllegalArgumentException();
        }

        retryClient.setHttpClient(httpClient);
        return retryClient;
    }


    private Function<String, String> rewriteSchemeFunction;
    private Function<String, String> rewriteUserInfoFunction;
    private Function<String, String> rewriteHostFunction;
    private Function<Integer, Integer> rewritePortFunction;
    private Function<String, String> rewritePathFunction;
    private Function<String, String> rewriteQueryFunction;
    private Function<String, String> rewriteFragmentFunction;

    private Predicate<Exception> ignoreExceptionPredicate;
    private Predicate<HttpRequestContext> successHttpResponsePredicate;


    /**
     * rewrite whole url，ignore {@link #rewriteSchemeFunction} {@link #rewriteHostFunction} {@link #rewritePathFunction} {@link #rewriteQueryFunction} {@link #rewriteFragmentFunction}
     */
    private Function<String, String> rewriteUrlFunction;

    private Function<String, String> rewriteBodyFunction;

    public RetryContext rewriteScheme(String overrideScheme) {
        return rewriteScheme(str -> overrideScheme);
    }

    public RetryContext rewriteScheme(Function<String, String> overrideSchemeFunction) {
        this.rewriteSchemeFunction = overrideSchemeFunction;
        return this;
    }

    public RetryContext rewriteUserInfo(String overrideUserInfo) {
        return rewriteUserInfo(str -> overrideUserInfo);
    }

    public RetryContext rewriteUserInfo(Function<String, String> overrideUserInfoFunction) {
        this.rewriteUserInfoFunction = overrideUserInfoFunction;
        return this;
    }

    public RetryContext rewriteHost(String overrideHost) {
        return rewriteHost(str -> overrideHost);
    }

    public RetryContext rewriteHost(Function<String, String> overrideHostFunction) {
        this.rewriteHostFunction = overrideHostFunction;
        return this;
    }

    public RetryContext rewritePort(Integer overridePort) {
        return rewritePort(integer -> overridePort);
    }

    public RetryContext rewritePort(Function<Integer, Integer> overridePortFunction) {
        this.rewritePortFunction = overridePortFunction;
        return this;
    }

    public RetryContext rewritePath(String overridePath) {
        return rewritePath(str -> overridePath);
    }

    public RetryContext rewritePath(Function<String, String> overridePathFunction) {
        this.rewritePathFunction = overridePathFunction;
        return this;
    }

    public RetryContext rewriteQuery(String overrideQuery) {
        return rewriteQuery(str -> overrideQuery);
    }

    public RetryContext rewriteQuery(Function<String, String> overrideQueryFunction) {
        this.rewriteQueryFunction = overrideQueryFunction;
        return this;
    }

    public RetryContext rewriteFragment(String overrideFragment) {
        return rewriteFragment(str -> overrideFragment);
    }

    public RetryContext rewriteFragment(Function<String, String> overrideFragmentFunction) {
        this.rewriteFragmentFunction = overrideFragmentFunction;
        return this;
    }

    public RetryContext rewriteUrl(String overrideUrl) {
        return rewriteUrl(str -> overrideUrl);
    }


    public RetryContext rewriteUrl(Function<String, String> overrideUrlFunction) {
        this.rewriteUrlFunction = overrideUrlFunction;
        return this;
    }


    // TODO: 2024/2/13 header should be List<String>
    /*public RetryContext rewriteHeaders(String headerKey, String headerValue) {
        return this;
    }*/

    public RetryContext rewriteBody(String overrideBody) {
        return rewriteBody(str -> overrideBody);
    }

    public RetryContext rewriteBody(Function<String, String> overrideBodyFunction) {
        this.rewriteBodyFunction = overrideBodyFunction;
        return this;
    }


    /**
     * Ignore given {@link Exception} types, still consider them as successful when these Exceptions occurred.
     */
    public RetryContext ignoreException(Class<Exception> ignoreException, Class<Exception>... moreIgnoreExceptions) {
        assert moreIgnoreExceptions != null;
        this.ignoreExceptionPredicate = (exp) -> Stream.concat(Stream.of(ignoreException), Stream.of(moreIgnoreExceptions)).noneMatch(exceptionClass -> exceptionClass.isAssignableFrom(exp.getClass()));
        return this;
    }

    /**
     * Predicate that returns true if the {@link Exception} occurred but still considered successful.
     */
    public RetryContext ignoreException(Predicate<Exception> ignoreExceptionPredicate) {
        this.ignoreExceptionPredicate = ignoreExceptionPredicate;
        return this;
    }

    /**
     * Checks if the response is successful.
     * When no custom Predicate is specified, it defaults to checking if the http status code is 2xx.
     *
     * @param successHttpResponsePredicate Predicate that returns true if the response is considered successful.
     */
    public RetryContext successWhenResponse(Predicate<HttpRequestContext> successHttpResponsePredicate) {
        this.successHttpResponsePredicate = successHttpResponsePredicate;
        return this;
    }


    /**
     * Build request url for execute.
     */
    public String buildRequestUrl() {
        URI uri = URI.create(requestLog.getRequestUrl());

        try {
            return new URI(
                    rewriteSchemeFunction != null ? rewriteSchemeFunction.apply(uri.getScheme()) : uri.getScheme(),
                    rewriteUserInfoFunction != null ? rewriteUserInfoFunction.apply(uri.getUserInfo()) : uri.getUserInfo(),
                    rewriteHostFunction != null ? rewriteHostFunction.apply(uri.getHost()) : uri.getHost(),
                    rewritePortFunction != null ? rewritePortFunction.apply(uri.getPort()) : uri.getPort(),
                    rewritePathFunction != null ? rewritePathFunction.apply(uri.getPath()) : uri.getPath(),
                    rewriteQueryFunction != null ? rewriteQueryFunction.apply(uri.getQuery()) : uri.getQuery(),
                    rewriteFragmentFunction != null ? rewriteFragmentFunction.apply(uri.getFragment()) : uri.getFragment()
            ).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }


    public Map<String, List<String>> buildRequestHeaders() {
        Map<String, List<String>> headers = requestLog.getRequestHeaders();
        // TODO: 2024/2/14 custom ignore headers
        //  custom rewrite headers like token
        //  default ignore headers like content-length
        return headers;
    }

    // TODO: 2024/2/14 if user wants to consume null, whether is request body is null
    public String buildRequestBody() {
        // 这里 body 如果为空的话，不会到 rewrite function 里

        if (!StringUtils.hasText(requestLog.getRequestBody())) {
            return null;
        }
        if (rewriteBodyFunction != null) {
            return rewriteBodyFunction.apply(requestLog.getRequestBody());
        }

        return requestLog.getRequestBody();
    }


}
