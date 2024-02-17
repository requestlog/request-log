package com.github.requestlog.okhttp;


import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import com.github.requestlog.core.support.function.SupplierExp;
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
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.stream.Stream;

import static com.github.requestlog.test.controller.TestRestController.*;
import static com.github.requestlog.test.util.ObjectUtil.asStringPretty;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = "debug=true"
)
@Slf4j
public class RequestLogOkHttpTests {

    @LocalServerPort
    private int port;

    @Autowired
    private OkHttpClient okHttpClient;

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
    public void testGet(String path, boolean expectsException, boolean enableLogging, int expectsLogIncrease) throws IOException {

        int size = inMemoryRequestLogRepository.getRequestLogSize();

        Request request = new Request.Builder()
                .url(String.format("http://localhost:%s%s", port, path))
                .build();

        SupplierExp<Response, IOException> supplierExp = () -> okHttpClient.newCall(request).execute();
        try (Response response = enableLogging ? LogContext.log().executeWithExp(supplierExp) : supplierExp.get()) {
            log.info("response code: {}, body: {}", response.code(), response.body() == null ? null : response.body().string());
        } catch (Exception e) {
            if (!expectsException) {
                throw e;
            }
            log.warn("occurred error (expected):", e);
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
                Arguments.of(GET_ERROR_PATH + "?a=2&b=3", false, true, 1)
        );
    }


    @DisplayName("Test http post")
    @ParameterizedTest
    @MethodSource("testPostArguments")
    public void testPost(String path, RequestBody requestBody, boolean expectsException, boolean enableLogging, int expectsLogIncrease) throws IOException {

        int size = inMemoryRequestLogRepository.getRequestLogSize();

        String url = String.format("http://localhost:%s%s", port, path);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        SupplierExp<Response, IOException> supplierExp = () -> okHttpClient.newCall(request).execute();

        try (Response response = enableLogging ? LogContext.log().executeWithExp(supplierExp) : supplierExp.get()) {
            log.info("response code: {}, body: {}", response.code(), response.body() == null ? null : response.body().string());
        } catch (Exception e) {
            if (!expectsException) {
                throw e;
            }
            log.warn("occurred error (expected):", e);
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
                Arguments.of(FORM_POST_PATH, createRandomFormBody(), false, false, 0),
                Arguments.of(FORM_POST_PATH, createRandomFormBody(), false, true, 0),
                Arguments.of(FORM_POST_ERROR_PATH, createRandomFormBody(), false, false, 0),
                Arguments.of(FORM_POST_ERROR_PATH, createRandomFormBody(), false, true, 1),

                Arguments.of(JSON_POST_PATH, RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()), false, false, 0),
                Arguments.of(JSON_POST_PATH, RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()), false, true, 0),
                Arguments.of(JSON_POST_ERROR_PATH, RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()), false, false, 0),
                Arguments.of(JSON_POST_ERROR_PATH, RequestBody.create(MediaType.parse("application/json"), RequestParamModel.randomJson()), false, true, 1)
        );
    }


    private static FormBody createRandomFormBody() {
        RequestParamModel randomModel = RequestParamModel.randomObj();

        FormBody.Builder builder =  new FormBody.Builder()
                .add("stringValue", randomModel.getStringValue())
                .add("intValue", String.valueOf(randomModel.getIntValue()))
                .add("booleanValue", String.valueOf(randomModel.getBooleanValue()));
        if (!CollectionUtils.isEmpty(randomModel.getStringList())) {
            randomModel.getStringList().forEach(value -> builder.add("stringList", value));
        }
        if (!CollectionUtils.isEmpty(randomModel.getIntegerList())) {
            randomModel.getIntegerList().forEach(value -> builder.add("integerList", String.valueOf(value)));
        }

        return builder.build();
    }


}
