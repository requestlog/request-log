package com.github.requestlog.resttemplate;


import com.github.requestlog.core.context.LogContext;
import com.github.requestlog.core.model.RequestLog;
import com.github.requestlog.core.model.RequestRetryJob;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static com.github.requestlog.test.controller.TestRestController.GET_ERROR_PATH;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestApplication.class,
        properties = "debug=true"
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
    @Test
    public void generateRetryJob() {

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
        log.info("retry job generated: {}", inMemoryRequestLogRepository.getLastRetryJob());
    }

}
