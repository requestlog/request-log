package com.github.requestlog.test.controller;


import com.github.requestlog.test.model.RequestParamModel;
import com.github.requestlog.test.model.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
public class TestRestController {

    private static final String PREFIX = "/test/rest";

    public static final String GET_PATH = PREFIX + "/get";
    public static final String GET_ERROR_PATH = PREFIX + "/getError";
    public static final String FORM_POST_PATH = PREFIX + "/formPost";
    public static final String FORM_POST_ERROR_PATH = PREFIX + "/formPostError";
    public static final String JSON_POST_PATH = PREFIX + "/jsonPost";
    public static final String JSON_POST_ERROR_PATH = PREFIX + "/jsonPostError";


    @GetMapping(GET_PATH)
    public ResponseModel get() {
        return new ResponseModel(200);
    }


    @GetMapping(GET_ERROR_PATH)
    public ResponseModel getError() {
        throw new RuntimeException("manually thrown exception 4 getError");
    }


    @PostMapping(value = FORM_POST_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseModel formPost(@ModelAttribute RequestParamModel param) {
        log.debug("formPost param: {}", param);
        return new ResponseModel(200);
    }

    @PostMapping(value = FORM_POST_ERROR_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseModel formPostError(@ModelAttribute RequestParamModel param) {
        log.debug("formPostError param: {}", param);
        throw new RuntimeException("manually thrown exception 4 formPostError");
    }

    @PostMapping(value = JSON_POST_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel jsonPost(@RequestBody RequestParamModel param) {
        log.debug("jsonPost param: {}", param);
        return new ResponseModel(200);
    }

    @PostMapping(value = JSON_POST_ERROR_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseModel jsonPostError(@RequestBody RequestParamModel param) {
        log.debug("jsonPostError param: {}", param);
        throw new RuntimeException("manually thrown exception 4 jsonPostError");
    }


}
