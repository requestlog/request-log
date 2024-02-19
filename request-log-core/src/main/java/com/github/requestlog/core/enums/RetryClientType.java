package com.github.requestlog.core.enums;


/**
 * Clients for retry, distinguish from {@link RequestContextType}
 */
public enum RetryClientType {

    REST_TEMPLATE,
    APACHE_HTTP_CLIENT,
    OK_HTTP,

}
