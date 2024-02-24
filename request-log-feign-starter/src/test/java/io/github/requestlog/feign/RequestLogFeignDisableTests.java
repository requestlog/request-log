package io.github.requestlog.feign;

import io.github.requestlog.core.context.LogContext;
import io.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import io.github.requestlog.feign.clients.TestFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * Tests for disable by properties.
 */
public class RequestLogFeignDisableTests {


    @SpringBootTest(classes = TestApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {
                    "debug=true"
            }
    )
    @Slf4j
    public static class NormalTests {

        @Autowired
        private TestFeignClient testFeignClient;
        @Autowired
        private InMemoryRequestLogRepository repository;

        @Test
        public void testNormal() {
            int size = repository.getRequestLogSize();
            LogContext.log().execute(() -> {
                try {
                    testFeignClient.getError();
                } catch (Exception ignored) {
                }
            });
            assert repository.getRequestLogSize() == size + 1;
        }

    }

    @SpringBootTest(classes = TestApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {
                    "debug=true",
                    "request-log.ok-http.disable=true"
            }
    )
    @Slf4j
    public static class DisableTests {

        @Autowired
        private TestFeignClient testFeignClient;
        @Autowired
        private InMemoryRequestLogRepository repository;

        @Test
        public void testDisable() {
            int size = repository.getRequestLogSize();
            LogContext.log().execute(() -> {
                try {
                    testFeignClient.getError();
                } catch (Exception ignored) {
                }
            });
            assert repository.getRequestLogSize() == size;
        }

    }


}
