package io.github.requestlog.feign.clients;


import io.github.requestlog.test.model.RequestParamModel;
import io.github.requestlog.test.model.ResponseModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static io.github.requestlog.test.controller.TestRestController.*;


/**
 * Feign client for test.
 */
@Component
@FeignClient(value = TestFeignClient.SERVICE_ID, contextId = "testFeignService")
public interface TestFeignClient {

    String SERVICE_ID = "request-log-feign-starter";

    @GetMapping(GET_PATH)
    ResponseModel get();

    @GetMapping(GET_ERROR_PATH)
    ResponseModel getError();

    @PostMapping(value = FORM_POST_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseModel formPost(@ModelAttribute RequestParamModel param);

    @PostMapping(value = FORM_POST_ERROR_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseModel formPostError(@ModelAttribute RequestParamModel param);

    @PostMapping(value = JSON_POST_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseModel jsonPost(@RequestBody RequestParamModel param);

    @PostMapping(value = JSON_POST_ERROR_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseModel jsonPostError(@RequestBody RequestParamModel param);

}
