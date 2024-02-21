package io.github.requestlog.feign;


import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import io.github.requestlog.feign.clients.TestFeignClient;
import io.github.requestlog.test.model.RequestParamModel;
import io.github.requestlog.test.model.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.requestlog.test.util.ObjectUtil.asStringPretty;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = "debug=true"
)
@Slf4j
public class RequestLogFeignTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestFeignClient testFeignClient;

    @Autowired
    private InMemoryRequestLogRepository inMemoryRequestLogRepository;


    /**
     * Test Http GET and POST method.
     *
     * @param feignClientFunction contains a feign client invoke
     * @param expectsException    indicates whether an exception is expected
     * @param enableLogging       indicates whether request logging is enabled
     * @param expectsLogIncrease  indicates whether logging increase is expected
     */
    @DisplayName("Test http get and post")
    @ParameterizedTest
    @MethodSource("testGetAndPostArguments")
    public void testGetAndPost(Function<TestFeignClient, ResponseModel> feignClientFunction, boolean expectsException, boolean enableLogging, int expectsLogIncrease) {

        int size = inMemoryRequestLogRepository.getRequestLogSize();

        try {
            Supplier<ResponseModel> supplier = () -> feignClientFunction.apply(testFeignClient);
            ResponseModel responseModel = enableLogging ? LogContext.log().execute(supplier) : supplier.get();
            log.info("response model: {}", responseModel);
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
     * Provides parameters for {@link #testGetAndPost}
     */
    static Stream<Arguments> testGetAndPostArguments() {
        Function<TestFeignClient, ResponseModel> getFunction = TestFeignClient::get;
        Function<TestFeignClient, ResponseModel> getErrorFunction = TestFeignClient::getError;
        Function<TestFeignClient, ResponseModel> formPostFunction = (feignClient) -> feignClient.formPost(RequestParamModel.randomObj());
        Function<TestFeignClient, ResponseModel> formPostErrorFunction = (feignClient) -> feignClient.formPostError(RequestParamModel.randomObj());
        Function<TestFeignClient, ResponseModel> jsonPostFunction = (feignClient) -> feignClient.jsonPost(RequestParamModel.randomObj());
        Function<TestFeignClient, ResponseModel> jsonPostErrorFunction = (feignClient) -> feignClient.jsonPostError(RequestParamModel.randomObj());

        return Stream.of(
                // get
                Arguments.of(getFunction, false, false, 0),
                Arguments.of(getFunction, false, true, 0),
                Arguments.of(getErrorFunction, true, false, 0),
                Arguments.of(getErrorFunction, true, true, 1),

                // form post
                Arguments.of(formPostFunction, false, false, 0),
                Arguments.of(formPostFunction, false, true, 0),
                Arguments.of(formPostErrorFunction, true, false, 0),
                Arguments.of(formPostErrorFunction, true, true, 1),

                // json post
                Arguments.of(jsonPostFunction, false, false, 0),
                Arguments.of(jsonPostFunction, false, true, 0),
                Arguments.of(jsonPostErrorFunction, true, false, 0),
                Arguments.of(jsonPostErrorFunction, true, true, 1)
        );

    }


}
