package com.github.requestlog.apachehttpclient.support.client.methods;

import com.github.requestlog.core.enums.HttpMethod;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;


/**
 * Custom HTTP request class, extending {@link HttpRequestBase},
 * designed to support various HTTP methods without a request body,
 * such as GET, HEAD, OPTIONS, etc.
 */
public class CustomNonBodyHttpRequest extends HttpRequestBase {

    private final HttpMethod method;

    public CustomNonBodyHttpRequest(String method, String uri) {
        this(method, URI.create(uri));
    }

    public CustomNonBodyHttpRequest(String method, URI uri) {
        this.method = HttpMethod.of(method);
        if (method == null) {
            throw new IllegalArgumentException("not supported method");
        }
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return method.name().toUpperCase();
    }

}
