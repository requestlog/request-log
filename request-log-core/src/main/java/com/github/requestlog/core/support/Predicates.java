package com.github.requestlog.core.support;

import com.github.requestlog.core.enums.RequestContextType;
import com.github.requestlog.core.model.HttpRequestContext;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;


/**
 * Some {@link Predicates}
 */
public class Predicates {


    /**
     * Default global ignore Exception predicate.
     */
    public static final Predicate<Exception> DEFAULT_IGNORE_EXCEPTION_PREDICATE = (exp) -> false;

    /**
     * Default global success http response predicate.
     */
    public static final Predicate<HttpRequestContext> DEFAULT_SUCCESS_HTTP_RESPONSE_PREDICATE = (requestContext) -> HttpUtils.isSuccess(requestContext.getResponseCode());


    /**
     * Custom global ignore exception predicate.
     * Overrides {@link #DEFAULT_IGNORE_EXCEPTION_PREDICATE}
     */
    private static Predicate<Exception> CUSTOM_IGNORE_EXCEPTION_PREDICATE = null;

    /**
     * Custom global success http response predicate.
     * Overrides {@link #DEFAULT_SUCCESS_HTTP_RESPONSE_PREDICATE}
     */
    private static Predicate<HttpRequestContext> CUSTOM_SUCCESS_HTTP_RESPONSE_PREDICATE = null;


    /**
     * Custom predicates for ignore exceptions, success http response.
     */
    private static final Map<RequestContextType, Predicate<Exception>> CUSTOM_IGNORE_EXCEPTION_PREDICATE_MAP = Collections.synchronizedMap(new HashMap<>(16));
    private static final Map<RequestContextType, Predicate<HttpRequestContext>> CUSTOM_SUCCESS_HTTP_RESPONSE_PREDICATE_MAP = Collections.synchronizedMap(new HashMap<>(16));


    /**
     * Register custom ignore exception predicate.
     * Overrides default predicates.
     *
     * @param ignoreExceptionPredicate Predicate for check exception.
     * @param requestContextTypes      The specific type for override, or null if no type restriction is needed.
     */
    public static void registerIgnoreExceptionPredicate(Predicate<Exception> ignoreExceptionPredicate, RequestContextType... requestContextTypes) {
        Assert.notNull(ignoreExceptionPredicate, "exceptionPredicate can not be null");
        if (requestContextTypes.length == 0) {
            CUSTOM_IGNORE_EXCEPTION_PREDICATE = ignoreExceptionPredicate;
        } else {
            for (RequestContextType requestContextType : requestContextTypes) {
                CUSTOM_IGNORE_EXCEPTION_PREDICATE_MAP.put(requestContextType, ignoreExceptionPredicate);
            }
        }
    }


    /**
     * Register custom http code and body predicate.
     * Overrides default predicates.
     *
     * @param successHttpResponsePredicate Predicate for check if http response success.
     * @param requestContextTypes          The specific type for override, or null if no type restriction.
     */
    public static void registerSuccessResponsePredicate(Predicate<HttpRequestContext> successHttpResponsePredicate, RequestContextType... requestContextTypes) {
        Assert.notNull(successHttpResponsePredicate, "httpRequestContextPredicate can not be null");
        if (requestContextTypes.length == 0) {
            CUSTOM_SUCCESS_HTTP_RESPONSE_PREDICATE = successHttpResponsePredicate;
        } else {
            for (RequestContextType requestContextType : requestContextTypes) {
                CUSTOM_SUCCESS_HTTP_RESPONSE_PREDICATE_MAP.put(requestContextType, successHttpResponsePredicate);
            }
        }
    }


    /**
     * Get exception predicate by {@link RequestContextType}, multiple candidate order by scope.
     */
    public static Predicate<Exception> getIgnoreExceptionPredicate(RequestContextType requestContextType) {
        return SupplierChain.of(CUSTOM_IGNORE_EXCEPTION_PREDICATE_MAP.get(requestContextType))
                .or(CUSTOM_IGNORE_EXCEPTION_PREDICATE)
                .or(DEFAULT_IGNORE_EXCEPTION_PREDICATE)
                .get();
    }

    /**
     * Get http response code and body predicate by {@link RequestContextType}, multiple candidate order by scope.
     */
    public static Predicate<HttpRequestContext> getSuccessHttpResponsePredicate(RequestContextType requestContextType) {
        return SupplierChain.of(CUSTOM_SUCCESS_HTTP_RESPONSE_PREDICATE_MAP.get(requestContextType))
                .or(CUSTOM_SUCCESS_HTTP_RESPONSE_PREDICATE)
                .or(DEFAULT_SUCCESS_HTTP_RESPONSE_PREDICATE)
                .get();
    }


}
