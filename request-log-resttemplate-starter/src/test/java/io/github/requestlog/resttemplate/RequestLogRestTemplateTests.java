package io.github.requestlog.resttemplate;


import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import io.github.requestlog.test.controller.TestRestController;
import io.github.requestlog.test.model.RequestParamModel;
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

import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.requestlog.test.util.ObjectUtil.asStringPretty;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = {
                "debug=true",
        }
)
@Slf4j
public class RequestLogRestTemplateTests {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InMemoryRequestLogRepository inMemoryRequestLogRepository;


    /**
     * Tests HTTP GET method.
     *
     * @param path               the request path
     * @param expectsException   indicates whether an exception is expected
     * @param enableLogging      indicates whether request logging is enabled
     * @param expectsLogIncrease indicates whether logging increase is expected
     */
    @DisplayName("Test http get")
    @ParameterizedTest
    @MethodSource("testGetArguments")
    public void testGet(String path, boolean expectsException, boolean enableLogging, int expectsLogIncrease) {

        // TODO: 2024/1/31 specify for current request Predicates.

        String url = String.format("http://localhost:%s%s", port, path);

        int size = inMemoryRequestLogRepository.getRequestLogSize();
        try {
            Supplier<String> supplier = () -> restTemplate.getForObject(url, String.class);

            String responseBody = enableLogging ? LogContext.log().execute(supplier) : supplier.get();
            log.info("response: {}", responseBody);
        } catch (Exception e) {
            if (!expectsException) {
                throw e;
            }
            log.warn("occurred error:", e);
        }

        assert inMemoryRequestLogRepository.getRequestLogSize() - size == expectsLogIncrease;

        if (expectsLogIncrease > 0) {
            log.info("last generated request-log: \n{}", asStringPretty(inMemoryRequestLogRepository.getLastRequestLog()));
        }

    }


    /**
     * Provides parameters for {@link #testGet}
     */
    static Stream<Arguments> testGetArguments() {

        return Stream.of(
                Arguments.of(TestRestController.GET_PATH, false, false, 0),
                Arguments.of(TestRestController.GET_PATH, false, true, 0),
                Arguments.of(TestRestController.GET_ERROR_PATH, true, false, 0),
                Arguments.of(TestRestController.GET_ERROR_PATH, true, true, 1)
        );
    }


    /**
     * Tests HTTP POST method.
     *
     * @param path               the request path
     * @param mediaType          request content-type
     * @param expectsException   indicates whether an exception is expected
     * @param enableLogging      indicates whether request logging is enabled
     * @param expectsLogIncrease indicates whether logging increase is expected
     */
    @DisplayName("Test http post")
    @ParameterizedTest
    @MethodSource("testPostArguments")
    public void testPost(String path, MediaType mediaType, boolean expectsException, boolean enableLogging, int expectsLogIncrease) {

        String url = String.format("http://localhost:%s%s", port, path);

        int size = inMemoryRequestLogRepository.getRequestLogSize();
        try {

            Supplier<ResponseEntity<String>> supplier = () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(mediaType);
                HttpEntity<RequestParamModel> requestEntity = new HttpEntity<>(RequestParamModel.randomObj(), headers);
                return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            };

            ResponseEntity<String> responseEntity = enableLogging ? LogContext.log().execute(supplier) : supplier.get();
            log.info("response code: {}, body: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
        } catch (Exception e) {
            if (!expectsException) {
                throw e;
            }
            log.warn("occurred error:", e);
        }

        assert inMemoryRequestLogRepository.getRequestLogSize() - size == expectsLogIncrease;

        if (expectsLogIncrease > 0) {
            log.info("last generated request-log: \n{}", asStringPretty(inMemoryRequestLogRepository.getLastRequestLog()));
        }

    }


    /**
     * Provides parameters for {@link #testPost}
     */
    static Stream<Arguments> testPostArguments() {
        return Stream.of(
                Arguments.of(TestRestController.FORM_POST_PATH, MediaType.APPLICATION_FORM_URLENCODED, false, false, 0),
                Arguments.of(TestRestController.FORM_POST_PATH, MediaType.APPLICATION_FORM_URLENCODED, false, true, 0),
                Arguments.of(TestRestController.FORM_POST_ERROR_PATH, MediaType.APPLICATION_FORM_URLENCODED, true, false, 0),
                Arguments.of(TestRestController.FORM_POST_ERROR_PATH, MediaType.APPLICATION_FORM_URLENCODED, true, true, 1),

                Arguments.of(TestRestController.JSON_POST_PATH, MediaType.APPLICATION_JSON, false, false, 0),
                Arguments.of(TestRestController.JSON_POST_PATH, MediaType.APPLICATION_JSON, false, true, 0),
                Arguments.of(TestRestController.JSON_POST_ERROR_PATH, MediaType.APPLICATION_JSON, true, false, 0),
                Arguments.of(TestRestController.JSON_POST_ERROR_PATH, MediaType.APPLICATION_JSON, true, true, 1)
        );
    }

}