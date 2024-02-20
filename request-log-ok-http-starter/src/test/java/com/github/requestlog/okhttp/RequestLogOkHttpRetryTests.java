package com.github.requestlog.okhttp;


import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.context.RetryContext;
import com.github.requestlog.core.context.retry.RetryResult;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import com.github.requestlog.core.support.HttpUtils;
import com.github.requestlog.okhttp.context.retry.OkHttpRetryClient;
import com.github.requestlog.test.model.RequestParamModel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.requestlog.okhttp.support.OkHttpTestUtils.createRandomFormBody;
import static com.github.requestlog.test.controller.TestRestController.*;
import static com.github.requestlog.test.util.ObjectUtil.asStringPretty;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = "debug=true"
)
@Slf4j
public class RequestLogOkHttpRetryTests {

    @LocalServerPort
    private int port;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private InMemoryRequestLogRepository repository;


    /**
     * Test retry with {@link OkHttpClient}
     *
     * @param errorRequestConsumer Input OkHttpClient and port then do a error request.
     * @param rewriteRequestPath   The overridden path used during retry.
     * @param retrySucceed         Indicates whether the retry was successful.
     */
    @DisplayName("Test retry with OK Http ")
    @ParameterizedTest
    @MethodSource("testRetryArguments")
    public void testRetry(BiConsumer<OkHttpClient, Integer> errorRequestConsumer, String rewriteRequestPath, boolean retrySucceed) {

        int requestLogSize = repository.getRequestLogSize();
        int retryJobSize = repository.getRequestRetryJobSize();

        // Perform error request and generate request log and request retry job
        try {
            LogContext.retry().execute(() -> errorRequestConsumer.accept(okHttpClient, port));
        } catch (Exception ignored) {
        }

        // Verify RequestLog and RequestRetryJob is created
        assert repository.getRequestLogSize() == requestLogSize + 1;
        assert repository.getRequestRetryJobSize() == retryJobSize + 1;

        RetryResult retryResult = RetryContext.create(repository.getLastRequestLog(), repository.getLastRetryJob())
                .rewritePath(rewriteRequestPath)
                .successWhenResponse(requestContext -> HttpUtils.isSuccess(requestContext.getResponseCode()))
                .with(OkHttpRetryClient.class, okHttpClient)
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

        BiFunction<String, Function<Request.Builder, Request.Builder>, BiConsumer<OkHttpClient, Integer>> biFunction = (path, requestBuilderFunction) -> {
            return (okHttpClient, port) -> {
                String url = String.format("http://localhost:%s%s", port, path);

                Request.Builder builder = new Request.Builder()
                        .url(url);
                builder = requestBuilderFunction.apply(builder);
                try (Response response = okHttpClient.newCall(builder.build()).execute()) {
                } catch (Exception ignored) {
                }
            };
        };


        return Stream.of(
                Arguments.of(biFunction.apply(GET_ERROR_PATH, Request.Builder::get), GET_ERROR_PATH, false),
                Arguments.of(biFunction.apply(GET_ERROR_PATH, Request.Builder::get), GET_PATH, true),

                Arguments.of(biFunction.apply(GET_ERROR_PATH, Request.Builder::head), GET_ERROR_PATH, false),
                Arguments.of(biFunction.apply(GET_ERROR_PATH, Request.Builder::head), GET_PATH, true),

                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, builder -> builder.post(createRandomFormBody())), FORM_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, builder -> builder.post(createRandomFormBody())), FORM_POST_PATH, true),

                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, builder -> builder.put(createRandomFormBody())), FORM_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(FORM_POST_ERROR_PATH, builder -> builder.put(createRandomFormBody())), FORM_POST_PATH, true),

                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, builder -> builder.post(RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()))), JSON_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, builder -> builder.post(RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()))), JSON_POST_PATH, true),

                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, builder -> builder.put(RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()))), JSON_POST_ERROR_PATH, false),
                Arguments.of(biFunction.apply(JSON_POST_ERROR_PATH, builder -> builder.put(RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()))), JSON_POST_PATH, true)
        );
    }


}
