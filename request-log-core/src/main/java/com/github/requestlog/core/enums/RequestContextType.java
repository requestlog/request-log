package com.github.requestlog.core.enums;


/**
 * HTTP request context type, representing the type of HTTP client.
 */
public enum RequestContextType {

    REST_TEMPLATE,
    FEIGN,
    SERVLET,
    APACHE_HTTP_CLIENT,
    OK_HTTP,

}
