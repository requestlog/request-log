package io.github.requestlog.feign.aop;

import feign.Request;
import feign.Response;
import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.context.RetryContext;
import io.github.requestlog.core.handler.AbstractRequestLogHandler;
import io.github.requestlog.feign.context.request.FeignRequestContext;
import io.github.requestlog.feign.support.FeignUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;


/**
 * RequestLog advice for Feign.
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class RequestLogFeignAdvice {


    private final AbstractRequestLogHandler requestLogHandler;

    @Value("${request-log.ok-http.disable:#{null}}")
    private String disable;


    @Around("execution(* feign.Client.execute(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        // Skipped, no logging is specified or current request contains retry.
        if (LogContext.THREAD_LOCAL.get() == null || RetryContext.THREAD_LOCAL.get() != null
                || "true".equals(disable)) {
            return joinPoint.proceed();
        }

        Request request = FeignUtils.findRequest(joinPoint.getArgs());
        if (request == null) {
            log.warn("can not find Request from arguments");
            return joinPoint.proceed();
        }

        try {
            Response responseObj = (Response) joinPoint.proceed();
            responseObj = FeignUtils.convertAsRepeatableRead(responseObj); // TODO: 2024/2/1 need switch 2 turn it off?
            requestLogHandler.handle(new FeignRequestContext(LogContext.THREAD_LOCAL.get(), request, responseObj));
            return responseObj;
        } catch (Exception e) {
            requestLogHandler.handle(new FeignRequestContext(LogContext.THREAD_LOCAL.get(), request, e));
            throw e;
        }

    }

}
