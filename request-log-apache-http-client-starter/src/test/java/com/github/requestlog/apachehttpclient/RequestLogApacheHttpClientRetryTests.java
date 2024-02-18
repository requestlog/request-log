package com.github.requestlog.apachehttpclient;


import com.github.requestlog.apachehttpclient.context.retry.ApacheHttpClientRetryClient;
import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.context.RetryContext;
import com.github.requestlog.core.context.retry.RetryResult;
import com.github.requestlog.core.enums.HttpMethod;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.github.requestlog.apachehttpclient.support.HttpClientTestUtils.createRandomFormEntity;
import static com.github.requestlog.apachehttpclient.support.HttpClientTestUtils.createRandomJsonStringEntity;
import static com.github.requestlog.test.controller.TestRestController.*;
import static com.github.requestlog.test.util.ObjectUtil.asStringPretty;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = {
                "debug=true",
        }
)
@Slf4j
public class RequestLogApacheHttpClientRetryTests {

    @LocalServerPort
    private int port;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private InMemoryRequestLogRepository repository;


    /**
     * Test retry with {@link HttpClient}
     *
     * @param errorRequestConsumer Input HttpClient and port then do a error request.
     * @param rewriteRequestPath   The overridden path used during retry.
     * @param retrySucceed         Indicates whether the retry was successful.
     */
    @DisplayName("Test retry with Apache Http Client")
    @ParameterizedTest
    @MethodSource("testRetryArguments")
    public void testRetry(BiConsumer<HttpClient, Integer> errorRequestConsumer, String rewriteRequestPath, boolean retrySucceed) {

        int requestLogSize = repository.getRequestLogSize();
        int retryJobSize = repository.getRequestRetryJobSize();

        // Perform error request and generate request log and request retry job
        try {
            LogContext.retry().execute(() -> errorRequestConsumer.accept(httpClient, port));
        } catch (Exception ignored) {
        }

        // Verify RequestLog and RequestRetryJob is created
        assert repository.getRequestLogSize() == requestLogSize + 1;
        assert repository.getRequestRetryJobSize() == retryJobSize + 1;

        RetryResult retryResult = RetryContext.create(repository.getLastRequestLog(), repository.getLastRetryJob())
                .rewritePath(rewriteRequestPath)
                .successWhenResponse((requestContext) ->
                        requestContext.getResponseCode() == 200
                                && (requestContext.getHttpMethod() != HttpMethod.GET || StringUtils.startsWithIgnoreCase(requestContext.getResponseBody(), "{\"code\":200"))
                )
                .with(ApacheHttpClientRetryClient.class, httpClient)
                .execute();

        assert retryResult.succeed() == retrySucceed;

        repository.saveRequestRetryLog(retryResult.generateRetryLog());
        log.info("last generated RequestLog: \n{}", asStringPretty(repository.getLastRequestLog()));
        log.info("last generated RequestRetryJob: \n{}", asStringPretty(repository.getLastRetryJob()));
        log.info("last generated RequestRetryLog: \n{}", asStringPretty(repository.getLastRetryLog()));
    }


    /**
     * Provides parameters for {@link #testRetry}
     */
    static Stream<Arguments> testRetryArguments() {

        BiFunction<String, HttpRequest, BiConsumer<HttpClient, Integer>> biFunction = (path, request) -> {
            return (httpClient, port) -> {
                URI uri = URI.create(String.format("http://localhost:%s%s", port, path));
                try {
                    httpClient.execute(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), request);
                } catch (IOException ignored) {
                }
            };
        };


        return Stream.of(
                Arguments.of(biFunction.apply(GET_ERROR_PATH, new HttpGet(GET_ERROR_PATH)), GET_ERROR_PATH, false),
                Arguments.of(biFunction.apply(GET_ERROR_PATH, new HttpGet(GET_ERROR_PATH)), GET_PATH, true),

                Arguments.of(biFunction.apply(GET_ERROR_PATH, new HttpHead(GET_ERROR_PATH)), GET_ERROR_PATH, false),
                Arguments.of(biFunction.apply(GET_ERROR_PATH, new HttpHead(GET_ERROR_PATH)), GET_PATH, true),

                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, RequestBuilder.post(FORM_POST_ERROR_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build()), FORM_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, RequestBuilder.post(FORM_POST_ERROR_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build()), FORM_POST_PATH, true),

                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, RequestBuilder.put(FORM_POST_ERROR_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build()), FORM_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, RequestBuilder.put(FORM_POST_ERROR_PATH).setHeader("content-type", APPLICATION_FORM_URLENCODED.getMimeType()).setEntity(createRandomFormEntity()).build()), FORM_POST_PATH, true),

                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, RequestBuilder.post(JSON_POST_ERROR_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build()), JSON_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, RequestBuilder.post(JSON_POST_ERROR_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build()), JSON_POST_PATH, true),

                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, RequestBuilder.patch(JSON_POST_ERROR_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build()), JSON_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, RequestBuilder.patch(JSON_POST_ERROR_PATH).setHeader("content-type", APPLICATION_JSON.getMimeType()).setEntity(createRandomJsonStringEntity()).build()), JSON_POST_PATH, true)
        );
    }

}
