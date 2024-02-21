package io.github.requestlog.resttemplate.support.spring;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Decorator for {@link ClientHttpResponse} that allows the body to be read multiple times.
 *
 * Due to the access limitation of {@link org.springframework.http.client.BufferingClientHttpResponseWrapper}.
 */
public class BodyCacheClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse response;

    // body cache
    private byte[] body;

    public BodyCacheClientHttpResponseWrapper(ClientHttpResponse response) {
        this.response = response;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return response.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (this.body == null) {
            this.body = StreamUtils.copyToByteArray(this.response.getBody());
        }
        return new ByteArrayInputStream(this.body);
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }
}

