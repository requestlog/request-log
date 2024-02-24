package io.github.requestlog.servlet.aop;

import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.servlet.annotation.ReqLog;
import io.github.requestlog.servlet.context.request.ServletRequestContext;
import io.github.requestlog.servlet.support.ServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * RequestLog advice for Servlet.
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class RequestLogServletAdvice {


    private static final Object EMPTY_OBJECT = new Object();
    private static final ThreadLocal<Object> THREAD_LOCAL = new InheritableThreadLocal<>();


    private final AbstractRequestLogHandler requestLogHandler;

    @Value("${request-log.servlet.disable:#{null}}")
    private String disable;

    @Around("@annotation(reqLog)")
    public Object aroundHandleException(ProceedingJoinPoint joinPoint, ReqLog reqLog) throws Throwable {

        if (THREAD_LOCAL.get() != null || ServletUtils.isCurrentRequestRetry()
                || "true".equals(disable)) {
            return joinPoint.proceed();
        }

        final long beforeExecuteTimeMillis = System.currentTimeMillis();

        try {
            THREAD_LOCAL.set(EMPTY_OBJECT);
            return joinPoint.proceed();
        } catch (Exception e) {
            requestLogHandler.handle(new ServletRequestContext(reqLog, RequestContextHolder.getRequestAttributes(), e, beforeExecuteTimeMillis));
            throw e;
        } finally {
            THREAD_LOCAL.remove();
        }

    }


}
