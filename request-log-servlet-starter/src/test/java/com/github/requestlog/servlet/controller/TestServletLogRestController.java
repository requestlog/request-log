package com.github.requestlog.servlet.controller;


import com.github.requestlog.servlet.annotation.ReqLog;
import com.github.requestlog.test.model.RequestParamModel;
import com.github.requestlog.test.model.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
public class TestServletLogRestController {

    private static final String PREFIX = "/test/servlet/rest";

    public static final String GET_PATH = PREFIX + "/get";
    public static final String GET_ERROR_PATH = PREFIX + "/getError";
    public static final String GET_ERROR_WITH_RETRY_PATH = PREFIX + "/getErrorWithRetry";
    public static final String GET_ERROR_WITH_CUSTOM_EXCEPTION = PREFIX + "/getErrorWithCustomException";
    public static final String FORM_POST_PATH = PREFIX + "/formPost";
    public static final String FORM_POST_ERROR_PATH = PREFIX + "/formPostError";
    public static final String JSON_POST_PATH = PREFIX + "/jsonPost";
    public static final String JSON_POST_ERROR_PATH = PREFIX + "/jsonPostError";


    @ReqLog
    @GetMapping(GET_PATH)
    public ResponseModel get() {
        return new ResponseModel(200);
    }

    @ReqLog
    @GetMapping(GET_ERROR_PATH)
    public ResponseModel getError() {
        throw new RuntimeException("manually thrown exception 4 getError");
    }

    @ReqLog(retry = true)
    @GetMapping(GET_ERROR_WITH_RETRY_PATH)
    public ResponseModel getErrorWithRetry() {
        throw new RuntimeException("manually thrown exception 4 getError");
    }


    /**
     * Accepts a full class name of type {@link Exception}, performs reflection, instantiates an instance, and throws an exception of that type.
     */
    @ReqLog(retry = true, whenException = IllegalArgumentException.class)
    @GetMapping(GET_ERROR_WITH_CUSTOM_EXCEPTION)
    public ResponseModel getErrorWithCustomException(@RequestParam(name = "fullClassName", required = false) String fullClassName) throws Exception {
        if (StringUtils.hasText(fullClassName)) {
            Exception exception = null;
            try {
                Class<?> clazz = ClassUtils.forName(fullClassName, getClass().getClassLoader());
                exception = (Exception) clazz.newInstance();
            } catch (Exception ignored) {
            }
            if (exception != null) {
                throw exception;
            }
        }
        throw new RuntimeException("manually thrown exception 4 getError");
    }


    @ReqLog
    @PostMapping(value = FORM_POST_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseModel formPost(@ModelAttribute RequestParamModel param) {
        log.debug("formPost param: {}", param);
        return new ResponseModel(200);
    }

    @ReqLog
    @PostMapping(value = FORM_POST_ERROR_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseModel formPostError(@ModelAttribute RequestParamModel param) {
        log.debug("formPostError param: {}", param);
        throw new RuntimeException("manually thrown exception 4 formPostError");
    }

    @ReqLog
    @PostMapping(value = JSON_POST_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel jsonPost(@RequestBody RequestParamModel param) {
        log.debug("jsonPost param: {}", param);
        return new ResponseModel(200);
    }

    @ReqLog
    @PostMapping(value = JSON_POST_ERROR_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel jsonPostError(@RequestBody RequestParamModel param) {
        log.debug("jsonPostError param: {}", param);
        throw new RuntimeException("manually thrown exception 4 jsonPostError");
    }

}
