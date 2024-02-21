package io.github.requestlog.apachehttpclient.support.client.methods;

import io.github.requestlog.core.enums.HttpMethod;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;

import java.net.URI;
import java.nio.charset.StandardCharsets;


/**
 * Custom HTTP request class, extending {@link HttpEntityEnclosingRequestBase},
 * designed to support various HTTP methods with a request body,
 * such as POST, PUT, PATCH, etc.
 */
public class CustomWithBodyHttpRequest extends HttpEntityEnclosingRequestBase {

    private final HttpMethod method;

    public CustomWithBodyHttpRequest(String method, String uri, String requestBody) {
        this(method, URI.create(uri), requestBody);
    }

    public CustomWithBodyHttpRequest(String method, URI uri, String requestBody) {
        this.method = HttpMethod.of(method);
        if (method == null) {
            throw new IllegalArgumentException("not supported method");
        }
        setURI(uri);
        setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
    }

    @Override
    public String getMethod() {
        return method.name().toUpperCase();
    }

}
