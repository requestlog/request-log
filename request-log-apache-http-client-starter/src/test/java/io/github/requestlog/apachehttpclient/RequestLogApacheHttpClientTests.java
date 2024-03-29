package io.github.requestlog.apachehttpclient;


import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import io.github.requestlog.core.support.function.SupplierExp;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.requestlog.apachehttpclient.support.HttpClientTestUtils.createRandomFormEntity;
import static io.github.requestlog.apachehttpclient.support.HttpClientTestUtils.createRandomJsonStringEntity;
import static io.github.requestlog.test.controller.TestRestController.*;
import static io.github.requestlog.test.util.ObjectUtil.asStringPretty;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = "debug=true"
)
@Slf4j
public class RequestLogApacheHttpClientTests {


    @LocalServerPort
    private int port;

    @Autowired
    private HttpClient httpClient;

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
    public void testGet(String path, boolean expectsException, boolean enableLogging, int expectsLogIncrease) throws Exception {

        String url = String.format("http://localhost:%s%s", port, path);

        int size = inMemoryRequestLogRepository.getRequestLogSize();

        try {
            SupplierExp<HttpResponse, IOException> supplierExp = () -> httpClient.execute(new HttpGet(url));
            HttpResponse response = enableLogging ? LogContext.log().executeWithExp(supplierExp) : supplierExp.get();
            log.info("response code: {}, body: {}", response.getStatusLine().getStatusCode(), StreamUtils.copyToString(response.getEntity().getContent(), StandardCharsets.UTF_8));
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
                Arguments.of(GET_PATH + "?a=1&b=2", false, false, 0),
                Arguments.of(GET_PATH, false, true, 0),
                Arguments.of(GET_ERROR_PATH, false, false, 0),
                Arguments.of(GET_ERROR_PATH + "?a=1&b=2", false, true, 1)
        );
    }


    /**
     * Tests HTTP POST method.
     *
     * @param httpHostFunction   takes port and returns an {@link HttpHost}
     * @param httpPost           {@link HttpHost} for execute
     * @param expectsException   indicates whether an exception is expected
     * @param enableLogging      indicates whether request logging is enabled
     * @param expectsLogIncrease indicates whether logging increase is expected
     */
    @DisplayName("Test http post")
    @ParameterizedTest
    @MethodSource("testPostArguments")
    public void testPost(Function<Integer, HttpHost> httpHostFunction, HttpUriRequest httpPost, boolean expectsException, boolean enableLogging, int expectsLogIncrease) throws IOException {

        int size = inMemoryRequestLogRepository.getRequestLogSize();

        try {
            SupplierExp<HttpResponse, IOException> supplierExp = () -> httpClient.execute(httpHostFunction.apply(port), httpPost);
            HttpResponse response = enableLogging ? LogContext.log().executeWithExp(supplierExp) : supplierExp.get();
            log.info("response code: {}, body: {}", response.getStatusLine().getStatusCode(), StreamUtils.copyToString(response.getEntity().getContent(), StandardCharsets.UTF_8));
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

        Function<Integer, HttpHost> httpHostFunction = (port) -> new HttpHost("localhost", port, "http");


        return Stream.of(
                Arguments.of(httpHostFunction, RequestBuilder.post(FORM_POST_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build(), false, false, 0),
                Arguments.of(httpHostFunction, RequestBuilder.post(FORM_POST_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build(), false, true, 0),
                Arguments.of(httpHostFunction, RequestBuilder.post(FORM_POST_ERROR_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build(), false, false, 0),
                Arguments.of(httpHostFunction, RequestBuilder.post(FORM_POST_ERROR_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build(), false, true, 1),

                Arguments.of(httpHostFunction, RequestBuilder.post(JSON_POST_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build(), false, false, 0),
                Arguments.of(httpHostFunction, RequestBuilder.post(JSON_POST_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build(), false, true, 0),
                Arguments.of(httpHostFunction, RequestBuilder.post(JSON_POST_ERROR_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build(), false, false, 0),
                Arguments.of(httpHostFunction, RequestBuilder.post(JSON_POST_ERROR_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build(), false, true, 1)

        );
    }


}
