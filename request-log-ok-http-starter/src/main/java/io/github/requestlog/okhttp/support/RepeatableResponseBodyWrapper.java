package io.github.requestlog.okhttp.support;


import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import java.io.IOException;


/**
 * Decorator for {@link ResponseBody} that allows it to be read multiple times.
 */
public class RepeatableResponseBodyWrapper extends ResponseBody {

    private final ResponseBody target;
    private Buffer buffer;

    public RepeatableResponseBodyWrapper(ResponseBody originalResponseBody) {
        this.target = originalResponseBody;
    }

    @Override
    public MediaType contentType() {
        return target.contentType();
    }

    @Override
    public long contentLength() {
        return target.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (buffer == null) {
            // Create a buffer and write the original ResponseBody content into it
            buffer = new Buffer();
            try {
                buffer.writeAll(target.source());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer.clone();
    }

    @Override
    public void close() {
        target.close();
    }

}
