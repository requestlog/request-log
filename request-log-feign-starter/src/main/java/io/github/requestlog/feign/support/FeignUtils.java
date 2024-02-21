package io.github.requestlog.feign.support;

import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Utility class for working with feign.
 */
@Slf4j
public class FeignUtils {


    /**
     * Convert headers from a map with collections to a map with lists.
     */
    public static Map<String, List<String>> convertHeaders(Map<String, Collection<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyMap();
        }
        return headers.keySet().stream().collect(Collectors.toMap(key -> key, key -> new ArrayList<>(headers.get(key))));
    }


    /**
     * Find {@link Request} from {@param args}
     */
    public static Request findRequest(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Request) {
                return (Request) arg;
            }
        }
        return null;
    }


    /**
     * Convert a Feign Response to a new response with a repeatable body.
     * If the body is already repeatable, return the original response without re-conversion.
     *
     * @param response Original Feign Response
     * @return Feign Response with a repeatable body
     */
    public static Response convertAsRepeatableRead(Response response) {

        // empty response body or body is already repeatable
        if (response == null || response.body() == null || response.body().isRepeatable()) {
            return response;
        }

        try {
            // Build a new response with the same status, reason, headers, and a repeatable body
            return Response.builder()
                    .status(response.status())
                    .reason(response.reason())
                    .headers(response.headers())
                    .body(StreamUtils.copyToByteArray(response.body().asInputStream()))
                    .request(response.request())
                    .build();
        } catch (IOException e) {
            log.warn("convertAsRepeatableRead failed", e);
            return response;
        }
    }

}
