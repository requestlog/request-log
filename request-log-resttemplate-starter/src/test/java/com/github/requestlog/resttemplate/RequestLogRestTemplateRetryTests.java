package com.github.requestlog.resttemplate;


import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.context.RetryContext;
import com.github.requestlog.core.context.retry.RetryResult;
import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import com.github.requestlog.core.support.HttpUtils;
import com.github.requestlog.resttemplate.context.retry.RestTemplateRetryClient;
import com.github.requestlog.test.model.RequestParamModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.github.requestlog.test.controller.TestRestController.*;
import static com.github.requestlog.test.util.ObjectUtil.asStringPretty;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = {
                "debug=true",
        }
)
@Slf4j
public class RequestLogRestTemplateRetryTests {


    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InMemoryRequestLogRepository inMemoryRequestLogRepository;


    /**
     * Generate a {@link RequestRetryJob} based on {@link RequestLog} and save it.
     *
     * Provides manual generation for scenarios where no retry task is set when generating logs.
     */
    @DisplayName("Test manually generate a RequestRetryJob")
    @Test
    public void testGenerateRetryJob() {

        int requestLogSize = inMemoryRequestLogRepository.getRequestLogSize();
        int retryJobSize = inMemoryRequestLogRepository.getRequestRetryJobSize();

        // Generate a request log with an error request
        try {
            LogContext.log().execute(() -> {
                restTemplate.getForObject(String.format("http://localhost:%s%s", port, GET_ERROR_PATH), String.class);
            });
        } catch (Exception ignored) {
        }


        // Verify that a log is generated but no retry job is created
        assert inMemoryRequestLogRepository.getRequestLogSize() == requestLogSize + 1;
        assert inMemoryRequestLogRepository.getRequestRetryJobSize() == retryJobSize;


        // Generate a retry job based on RequestLog and save it
        RequestLog requestLog = inMemoryRequestLogRepository.getLastRequestLog();
        RequestRetryJob generatedRetryJob = inMemoryRequestLogRepository.generateNewRetryJob(requestLog);
        inMemoryRequestLogRepository.saveRequestRetryJob(generatedRetryJob);

        // Verify that a retry job is generated and saved
        assert inMemoryRequestLogRepository.getRequestRetryJobSize() == retryJobSize + 1;
        log.info("retry job generated: \n{}", asStringPretty(inMemoryRequestLogRepository.getLastRetryJob()));
    }


    /**
     * Tests retry with {@link RestTemplate}
     *
     * @param errorRequestConsumer Input RestTemplate and port then do a error request.
     * @param retryRequestPath     The overridden path used during retry.
     * @param retrySucceed         Indicates whether the retry was successful.
     */
    @DisplayName("Test retry with RestTemplate")
    @ParameterizedTest
    @MethodSource("testRetryArguments")
    public void testRetry(BiConsumer<RestTemplate, Integer> errorRequestConsumer, String retryRequestPath, boolean retrySucceed) {

        int requestLogSize = inMemoryRequestLogRepository.getRequestLogSize();
        int retryJobSize = inMemoryRequestLogRepository.getRequestRetryJobSize();

        // Generate a request log with an error request
        try {
            LogContext.retry().execute(() -> {
                errorRequestConsumer.accept(restTemplate, port);
            });
        } catch (Exception ignored) {
        }

        // Verify RequestLog and RequestRetryJob is created
        assert inMemoryRequestLogRepository.getRequestLogSize() == requestLogSize + 1;
        assert inMemoryRequestLogRepository.getRequestRetryJobSize() == retryJobSize + 1;


        RetryResult retryResult = RetryContext.create(inMemoryRequestLogRepository.getLastRequestLog(), inMemoryRequestLogRepository.getLastRetryJob())
                .rewritePath(retryRequestPath) // maybe same with original request
                .successWhenResponse((requestContext) -> HttpUtils.isSuccess(requestContext.getResponseCode()))
                .with(RestTemplateRetryClient.class, restTemplate)
                .execute();

        assert retryResult.succeed() == retrySucceed;

        // TODO: 2024/2/15
        RequestRetryJob requestRetryJob = retryResult.updateRetryJob();

        // Generate and save current retry log.
        inMemoryRequestLogRepository.saveRequestRetryLog(retryResult.generateRetryLog());

        log.info("last generated RequestLog: \n{}", asStringPretty(inMemoryRequestLogRepository.getLastRequestLog()));
        log.info("last generated RequestRetryJob: \n{}", asStringPretty(inMemoryRequestLogRepository.getLastRetryJob()));
        log.info("last generated RequestRetryLog: \n{}", asStringPretty(inMemoryRequestLogRepository.getLastRetryLog()));

    }


    /**
     * Provides parameters for {@link #testRetry}
     */
    static Stream<Arguments> testRetryArguments() {

        BiFunction<String, MediaType, BiConsumer<RestTemplate, Integer>> biFunction = (path, mediaType) -> {
            return (restTemplate, port) -> {
                String url = String.format("http://localhost:%s%s", port, path);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(mediaType);
                HttpEntity<RequestParamModel> requestEntity = new HttpEntity<>(RequestParamModel.randomObj(), headers);
                restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            };
        };


        return Stream.of(
                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, MediaType.APPLICATION_FORM_URLENCODED), FORM_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, MediaType.APPLICATION_FORM_URLENCODED), FORM_POST_PATH, true),

                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, MediaType.APPLICATION_JSON), JSON_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, MediaType.APPLICATION_JSON), JSON_POST_PATH, true)
        );
    }

}
