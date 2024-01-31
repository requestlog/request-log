package com.github.requestlog.core.support;

import com.github.requestlog.core.enums.RequestContextType;
import com.github.requestlog.core.model.HttpRequestContextModel;
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
     * Default global Exception predicate.
     */
    public static final Predicate<Exception> DEFAULT_EXCEPTION_PREDICATE = (exp) -> true;

    /**
     * Default global http response predicate.
     */
    public static final Predicate<HttpRequestContextModel> DEFAULT_HTTP_RESPONSE_PREDICATE = (requestContext) -> requestContext.getResponseCode() == null || !requestContext.getResponseCode().equals(200);


    /**
     * Custom global exception predicate.
     * Overrides {@link #DEFAULT_EXCEPTION_PREDICATE}
     */
    private static Predicate<Exception> CUSTOM_EXCEPTION_PREDICATE = null;

    /**
     * Custom global http response predicate.
     * Overrides {@link #DEFAULT_HTTP_RESPONSE_PREDICATE}
     */
    private static Predicate<HttpRequestContextModel> CUSTOM_HTTP_RESPONSE_PREDICATE = null;


    /**
     * Custom predicates for exceptions, http response.
     */
    private static final Map<RequestContextType, Predicate<Exception>> CUSTOM_EXCEPTION_PREDICATE_MAP = Collections.synchronizedMap(new HashMap<>(16));
    private static final Map<RequestContextType, Predicate<HttpRequestContextModel>> CUSTOM_HTTP_RESPONSE_PREDICATE_MAP = Collections.synchronizedMap(new HashMap<>(16));


    /**
     * Register custom exception predicate.
     * Overrides default predicates.
     *
     * @param exceptionPredicate  Predicate for check exception.
     * @param requestContextTypes The specific type for override, or null if no type restriction is needed.
     */
    public static void registerException(Predicate<Exception> exceptionPredicate, RequestContextType... requestContextTypes) {
        Assert.notNull(exceptionPredicate, "exceptionPredicate can not be null");
        if (requestContextTypes.length == 0) {
            CUSTOM_EXCEPTION_PREDICATE = exceptionPredicate;
        } else {
            for (RequestContextType requestContextType : requestContextTypes) {
                CUSTOM_EXCEPTION_PREDICATE_MAP.put(requestContextType, exceptionPredicate);
            }
        }
    }


    /**
     * Register custom http code and body predicate.
     * Overrides default predicates.
     *
     * @param httpRequestContextPredicate Predicate for check http response.
     * @param requestContextTypes         The specific type for override, or null if no type restriction.
     */
    public static void registerResponse(Predicate<HttpRequestContextModel> httpRequestContextPredicate, RequestContextType... requestContextTypes) {
        Assert.notNull(httpRequestContextPredicate, "httpRequestContextPredicate can not be null");
        if (requestContextTypes.length == 0) {
            CUSTOM_HTTP_RESPONSE_PREDICATE = httpRequestContextPredicate;
        } else {
            for (RequestContextType requestContextType : requestContextTypes) {
                CUSTOM_HTTP_RESPONSE_PREDICATE_MAP.put(requestContextType, httpRequestContextPredicate);
            }
        }
    }


    /**
     * Get exception predicate by {@link RequestContextType}, multiple candidate order by scope.
     */
    public static Predicate<Exception> getExceptionPredicate(RequestContextType requestContextType) {
        return SupplierChain.of(CUSTOM_EXCEPTION_PREDICATE_MAP.get(requestContextType))
                .or(CUSTOM_EXCEPTION_PREDICATE)
                .or(DEFAULT_EXCEPTION_PREDICATE)
                .get();
    }

    /**
     * Get http response code and body predicate by {@link RequestContextType}, multiple candidate order by scope.
     */
    public static Predicate<HttpRequestContextModel> getResponsePredicate(RequestContextType requestContextType) {
        return SupplierChain.of(CUSTOM_HTTP_RESPONSE_PREDICATE_MAP.get(requestContextType))
                .or(CUSTOM_HTTP_RESPONSE_PREDICATE)
                .or(DEFAULT_HTTP_RESPONSE_PREDICATE)
                .get();
    }


}
