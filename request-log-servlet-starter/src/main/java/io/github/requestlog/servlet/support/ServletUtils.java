package io.github.requestlog.servlet.support;

import io.github.requestlog.servlet.support.http.BodyRepeatableReadCapability;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static io.github.requestlog.core.constant.Constants.RETRY_HEADER;


/**
 * Utility class for working with {@link Servlet}.
 */
public class ServletUtils {


    private final static Set<String> REQUEST_CONTENT_REPEATABLE_READ_FQN_SET = new HashSet<>();
    private final static Set<String> RESPONSE_CONTENT_REPEATABLE_READ_FQN_SET = new HashSet<>();

    static {
        REQUEST_CONTENT_REPEATABLE_READ_FQN_SET.add(ContentCachingRequestWrapper.class.getName());
        RESPONSE_CONTENT_REPEATABLE_READ_FQN_SET.add(ContentCachingResponseWrapper.class.getName());
    }


    /**
     * Checks if the body of the given {@link ServletRequest} is repeatable.
     */
    public static boolean isBodyRepeatableRead(ServletRequest request) {
        if (REQUEST_CONTENT_REPEATABLE_READ_FQN_SET.contains(request.getClass().getName())) {
            return true;
        }
        if (request instanceof BodyRepeatableReadCapability) {
            return ((BodyRepeatableReadCapability) request).isBodyRepeatableRead();
        }
        return false;
    }


    /**
     * Checks if the body of the given {@link ServletResponse} is repeatable.
     */
    public static boolean isBodyRepeatableRead(ServletResponse response) {
        if (RESPONSE_CONTENT_REPEATABLE_READ_FQN_SET.contains(response.getClass().getName())) {
            return true;
        }
        if (response instanceof BodyRepeatableReadCapability) {
            return ((BodyRepeatableReadCapability) response).isBodyRepeatableRead();
        }
        return false;
    }


    /**
     * Extracts request headers from the given {@link HttpServletRequest}.
     */
    public static Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> headersMap = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            headersMap.put(headerName, headerValues);
        }

        return headersMap;
    }


    /**
     * Extracts response headers from the given {@link HttpServletResponse}
     */
    public static Map<String, List<String>> getResponseHeaders(HttpServletResponse response) {
        if (response == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> headersMap = new HashMap<>();

        for (String headerName : response.getHeaderNames()) {
            headersMap.put(headerName, new ArrayList<>(response.getHeaders(headerName)));
        }
        return headersMap;
    }


    /**
     * Check if the current request is a retry request based on the request headers.
     */
    public static boolean isCurrentRequestRetry() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return false;
        }
        return StringUtils.hasText(((ServletRequestAttributes) requestAttributes).getRequest().getHeader(RETRY_HEADER));
    }

}
