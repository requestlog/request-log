package com.github.requestlog.servlet;


import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import com.github.requestlog.test.model.RequestParamModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.github.requestlog.servlet.controller.TestServletLogRestController.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = "debug=true"
)
@Slf4j
public class RequestLogServletTests {

    @Autowired
    private ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InMemoryRequestLogRepository inMemoryRequestLogRepository;


    /**
     * Tests HTTP GET method.
     *
     * @param getFunction        input RestTemplate and port then do request.
     * @param expectsException   indicates whether an exception is expected
     * @param expectsLogIncrease indicates whether logging increase is expected
     */
    @DisplayName("Test http get")
    @ParameterizedTest
    @MethodSource("testGetArguments")
    public void testGet(BiFunction<RestTemplate, Integer, String> getFunction, boolean expectsException, int expectsLogIncrease) {

        int size = inMemoryRequestLogRepository.getRequestLogSize();

        try {
            String responseBody = getFunction.apply(restTemplate, port);
            log.info("response: {}", responseBody);
        } catch (Exception e) {
            if (!expectsException) {
                throw e;
            }
            log.warn("occurred error:", e);
        }

        assert inMemoryRequestLogRepository.getRequestLogSize() - size == expectsLogIncrease;

        if (expectsLogIncrease > 0) {
            log.info("last generated request-log: {}", inMemoryRequestLogRepository.getLastRequestLog());
        }

    }

    /**
     * Provides parameters for {@link #testGet}
     */
    static Stream<Arguments> testGetArguments() {

        String urlTemplate = "http://localhost:%s%s";
        BiFunction<Integer, String, String> urlFunction = (port, path) -> String.format(urlTemplate, port, path);

        BiFunction<RestTemplate, Integer, String> getFunction = (restTemplate, port) -> restTemplate.getForObject(urlFunction.apply(port, GET_PATH), String.class);
        BiFunction<RestTemplate, Integer, String> getErrorFunction = (restTemplate, port) -> restTemplate.getForObject(urlFunction.apply(port, GET_ERROR_PATH), String.class);
        BiFunction<RestTemplate, Integer, String> getErrorWithRetryFunction = (restTemplate, port) -> restTemplate.getForObject(urlFunction.apply(port, GET_ERROR_WITH_RETRY_PATH), String.class);
        BiFunction<RestTemplate, Integer, String> getErrorWithRuntimeExceptionFunction = (restTemplate, port) -> {
            return restTemplate.getForObject(urlFunction.apply(port, GET_ERROR_WITH_CUSTOM_EXCEPTION) + "?fullClassName=" + RuntimeException.class.getName(), String.class);
        };
        BiFunction<RestTemplate, Integer, String> getErrorWithIllegalArgumentExceptionFunction = (restTemplate, port) -> {
            return restTemplate.getForObject(urlFunction.apply(port, GET_ERROR_WITH_CUSTOM_EXCEPTION) + "?fullClassName=" + IllegalArgumentException.class.getName(), String.class);
        };

        return Stream.of(
                Arguments.of(getFunction, false, 0),
                Arguments.of(getErrorFunction, true, 1),
                Arguments.of(getErrorWithRetryFunction, true, 1),
                Arguments.of(getErrorWithRuntimeExceptionFunction, true, 0),
                Arguments.of(getErrorWithIllegalArgumentExceptionFunction, true, 1)
        );
    }


    /**
     * Tests HTTP POST method.
     *
     * @param postFunction       input RestTemplate and port then do request.
     * @param expectsException   indicates whether an exception is expected
     * @param expectsLogIncrease indicates whether logging increase is expected
     */
    @DisplayName("Test http post")
    @ParameterizedTest
    @MethodSource("testPostArguments")
    public void testPost(BiFunction<RestTemplate, Integer, ResponseEntity<String>> postFunction, boolean expectsException, int expectsLogIncrease) {
        int size = inMemoryRequestLogRepository.getRequestLogSize();
        try {
            ResponseEntity<String> response = postFunction.apply(restTemplate, port);
            log.info("response code: {}, body: {}", response.getStatusCodeValue(), response.getBody());
        } catch (Exception e) {
            if (!expectsException) {
                throw e;
            }
            log.warn("occurred error:", e);
        }

        assert inMemoryRequestLogRepository.getRequestLogSize() - size == expectsLogIncrease;

        if (expectsLogIncrease > 0) {
            log.info("last generated request-log: {}", inMemoryRequestLogRepository.getLastRequestLog());
        }

    }


    /**
     * Provides parameters for {@link #testPost}
     */
    static Stream<Arguments> testPostArguments() {

        String urlTemplate = "http://localhost:%s%s";
        BiFunction<Integer, String, String> urlFunction = (port, path) -> String.format(urlTemplate, port, path);

        HttpHeaders formHeaders = new HttpHeaders();
        formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<RequestParamModel> formRequestEntity = new HttpEntity<>(RequestParamModel.randomObj(), formHeaders);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestParamModel> jsonRequestEntity = new HttpEntity<>(RequestParamModel.randomObj(), jsonHeaders);

        BiFunction<RestTemplate, Integer, ResponseEntity<String>> formPostFunction = (restTemplate, port) -> {
            return restTemplate.exchange(urlFunction.apply(port, FORM_POST_PATH), HttpMethod.POST, formRequestEntity, String.class);
        };
        BiFunction<RestTemplate, Integer, ResponseEntity<String>> formPostErrorFunction = (restTemplate, port) -> {
            return restTemplate.exchange(urlFunction.apply(port, FORM_POST_ERROR_PATH), HttpMethod.POST, formRequestEntity, String.class);
        };
        BiFunction<RestTemplate, Integer, ResponseEntity<String>> jsonPostFunction = (restTemplate, port) -> {
            return restTemplate.exchange(urlFunction.apply(port, JSON_POST_PATH), HttpMethod.POST, jsonRequestEntity, String.class);
        };
        BiFunction<RestTemplate, Integer, ResponseEntity<String>> jsonPostErrorFunction = (restTemplate, port) -> {
            return restTemplate.exchange(urlFunction.apply(port, JSON_POST_ERROR_PATH), HttpMethod.POST, jsonRequestEntity, String.class);
        };

        return Stream.of(
                Arguments.of(formPostFunction, false, 0),
                Arguments.of(formPostErrorFunction, true, 1),
                Arguments.of(jsonPostFunction, false, 0),
                Arguments.of(jsonPostErrorFunction, true, 1)
        );
    }

}
