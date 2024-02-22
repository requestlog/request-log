package io.github.requestlog.core.context;

import io.github.requestlog.core.context.retry.RetryClient;
import io.github.requestlog.core.model.HttpRequestContext;
import io.github.requestlog.core.model.RequestLog;
import io.github.requestlog.core.model.RequestRetryJob;
import io.github.requestlog.core.repository.IRequestLogRepository;
import io.github.requestlog.core.support.CollectionUtils;
import io.github.requestlog.core.support.HttpUtils;
import io.github.requestlog.core.support.tuples.Tuple2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Getter
public class RetryContext {

    /**
     * ThreadLocal for {@link RetryContext}
     */
    public static final ThreadLocal<RetryContext> THREAD_LOCAL = new InheritableThreadLocal<>();


    private final RequestLog requestLog;
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
    private Function<String, String> rewriteUrlFunction;

    private final List<Tuple2<String, String>> rewriteHeadersList = new ArrayList<>();
    private final List<Tuple2<String, String>> appendHeadersList = new ArrayList<>();

    private Function<String, String> rewriteBodyFunction;


    private Predicate<Exception> ignoreExceptionPredicate;
    private Predicate<HttpRequestContext> successHttpResponsePredicate;


    /**
     * Rewrites the scheme of the URL using the specified override value.
     */
    public RetryContext rewriteScheme(String overrideScheme) {
        return rewriteScheme(str -> overrideScheme);
    }

    /**
     * Rewrites the scheme of the URL using the provided function.
     */
    public RetryContext rewriteScheme(Function<String, String> overrideSchemeFunction) {
        this.rewriteSchemeFunction = overrideSchemeFunction;
        return this;
    }

    /**
     * Rewrites the user information of the URL using the specified override value.
     */
    public RetryContext rewriteUserInfo(String overrideUserInfo) {
        return rewriteUserInfo(str -> overrideUserInfo);
    }

    /**
     * Rewrites the user information of the URL using the provided function.
     */
    public RetryContext rewriteUserInfo(Function<String, String> overrideUserInfoFunction) {
        this.rewriteUserInfoFunction = overrideUserInfoFunction;
        return this;
    }

    /**
     * Rewrites the host of the URL using the specified override value.
     */
    public RetryContext rewriteHost(String overrideHost) {
        return rewriteHost(str -> overrideHost);
    }

    /**
     * Rewrites the host of the URL using the provided function.
     */
    public RetryContext rewriteHost(Function<String, String> overrideHostFunction) {
        this.rewriteHostFunction = overrideHostFunction;
        return this;
    }

    /**
     * Rewrites the port of the URL using the specified override value.
     */
    public RetryContext rewritePort(Integer overridePort) {
        return rewritePort(integer -> overridePort);
    }

    /**
     * Rewrites the port of the URL using the provided function.
     */
    public RetryContext rewritePort(Function<Integer, Integer> overridePortFunction) {
        this.rewritePortFunction = overridePortFunction;
        return this;
    }

    /**
     * Rewrites the path of the URL using the specified override value.
     */
    public RetryContext rewritePath(String overridePath) {
        return rewritePath(str -> overridePath);
    }

    /**
     * Rewrites the path of the URL using the provided function.
     */
    public RetryContext rewritePath(Function<String, String> overridePathFunction) {
        this.rewritePathFunction = overridePathFunction;
        return this;
    }

    /**
     * Rewrites the query of the URL using the specified override value.
     */
    public RetryContext rewriteQuery(String overrideQuery) {
        return rewriteQuery(str -> overrideQuery);
    }

    /**
     * Rewrites the query of the URL using the provided function.
     */
    public RetryContext rewriteQuery(Function<String, String> overrideQueryFunction) {
        this.rewriteQueryFunction = overrideQueryFunction;
        return this;
    }

    /**
     * Rewrites the fragment of the URL using the specified override value.
     */
    public RetryContext rewriteFragment(String overrideFragment) {
        return rewriteFragment(str -> overrideFragment);
    }

    /**
     * Rewrites the fragment of the URL using the provided function.
     */
    public RetryContext rewriteFragment(Function<String, String> overrideFragmentFunction) {
        this.rewriteFragmentFunction = overrideFragmentFunction;
        return this;
    }

    /**
     * Rewrites the entire URL using the specified override value.
     *
     * This operation will ignores other rewrite url methods like {@link #rewriteHost} {@link #rewritePath}.
     */
    public RetryContext rewriteUrl(String overrideUrl) {
        return rewriteUrl(str -> overrideUrl);
    }


    /**
     * Rewrites the entire URL using the provided function.
     *
     * This operation will ignores other rewrite url methods like {@link #rewriteHost} {@link #rewritePath}.
     */
    public RetryContext rewriteUrl(Function<String, String> overrideUrlFunction) {
        this.rewriteUrlFunction = overrideUrlFunction;
        return this;
    }


    // TODO: 2024/2/22 remove header ?

    /**
     * Rewrites the specified HTTP header with the given value.
     * If the header already exists, it will be overwritten with the new value.
     */
    public RetryContext rewriteHeader(String headerKey, String headerValue) {
        if (!StringUtils.hasText(headerValue) || !HttpUtils.shouldSpecifyManually(headerKey) || !StringUtils.hasText(headerValue)) {
            return this;
        }
        rewriteHeadersList.add(Tuple2.of(headerKey, headerValue));
        return this;
    }


    /**
     * Appends an additional HTTP header with the given value.
     */
    public RetryContext appendHeader(String headerKey, String headerValue) {
        if (!HttpUtils.shouldSpecifyManually(headerKey) || !StringUtils.hasText(headerValue)) {
            return this;
        }
        this.appendHeadersList.add(Tuple2.of(headerKey, headerValue));
        return this;
    }


    /**
     * Rewrites the entire body using the specified override value.
     */
    public RetryContext rewriteBody(String overrideBody) {
        return rewriteBody(str -> overrideBody);
    }

    /**
     * Rewrites the entire body using the provided function.
     */
    public RetryContext rewriteBody(Function<String, String> overrideBodyFunction) {
        this.rewriteBodyFunction = overrideBodyFunction;
        return this;
    }


    /**
     * Ignore given {@link Exception} types, still consider them as successful when these Exceptions occurred.
     */
    @SafeVarargs
    public final RetryContext ignoreException(Class<? extends Exception> ignoreException, Class<? extends Exception>... moreIgnoreExceptions) {
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


    /**
     * Build request headers.
     */
    public Map<String, List<String>> buildRequestHeaders() {

        Map<String, List<String>> originalHeaders = Optional.ofNullable(requestLog.getRequestHeaders()).orElse(new HashMap<>());

        // Create a copy of headers, filtering out headers that should not be manually specified.
        final Map<String, List<String>> newHeaders = CollectionUtils.filterWithKey(originalHeaders, e -> !HttpUtils.autoGeneratedHeader(e));

        Map<String, String> lowerCaseKeyNameMap = newHeaders.keySet().stream().filter(StringUtils::hasText).collect(Collectors.toMap(String::toLowerCase, Function.identity()));

        // rewrite headers
        if (!CollectionUtils.isEmpty(this.rewriteHeadersList)) {
            this.rewriteHeadersList.forEach(tuple2 -> {
                String originalKeyName = lowerCaseKeyNameMap.get(tuple2.getT1().toLowerCase());

                // Considering the append list below, we avoid using unmodifiable Collections.singletonList here.
                newHeaders.put(StringUtils.hasText(originalKeyName) ? originalKeyName : tuple2.getT1(), Arrays.asList(tuple2.getT2()));
            });
        }

        // append headers
        if (!CollectionUtils.isEmpty(this.appendHeadersList)) {
            this.appendHeadersList.forEach(tuple2 -> {
                String originalKeyName = lowerCaseKeyNameMap.get(tuple2.getT1().toLowerCase());
                newHeaders.computeIfAbsent(StringUtils.hasText(originalKeyName) ? originalKeyName : tuple2.getT1(), e -> new ArrayList<>())
                        .add(tuple2.getT2());
            });
        }

        return newHeaders;
    }


    /**
     * Build request body.
     * If the original body is null, still can be override by the specified value.
     */
    public String buildRequestBody() {

        if (rewriteBodyFunction != null) {
            return rewriteBodyFunction.apply(requestLog.getRequestBody());
        }

        return requestLog.getRequestBody();
    }


}
