package io.github.requestlog.servlet;


import io.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static io.github.requestlog.servlet.controller.TestServletLogRestController.GET_ERROR_PATH;

/**
 * Tests for disable by properties.
 */
public class RequestLogServletDisableTests {


    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            classes = TestApplication.class,
            properties = "debug=true"
    )
    @Slf4j
    public static class NormalTests {
        @LocalServerPort
        private int port;
        @Autowired
        private RestTemplate restTemplate;
        @Autowired
        private InMemoryRequestLogRepository repository;

        @Test
        public void testNormal() {
            int size = repository.getRequestLogSize();
            try {
                restTemplate.getForObject(String.format("http://localhost:%s%s", port, GET_ERROR_PATH), String.class);
            } catch (Exception ignored) {
            }
            assert repository.getRequestLogSize() == size + 1;
        }

    }

    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            classes = TestApplication.class,
            properties = {
                    "debug=true",
                    "request-log.servlet.disable=true"
            }
    )
    @Slf4j
    public static class DisableTests {
        @LocalServerPort
        private int port;
        @Autowired
        private RestTemplate restTemplate;
        @Autowired
        private InMemoryRequestLogRepository repository;

        @Test
        public void testDisable() {
            int size = repository.getRequestLogSize();
            try {
                restTemplate.getForObject(String.format("http://localhost:%s%s", port, GET_ERROR_PATH), String.class);
            } catch (Exception ignored) {
            }
            assert repository.getRequestLogSize() == size;
        }
    }

}
