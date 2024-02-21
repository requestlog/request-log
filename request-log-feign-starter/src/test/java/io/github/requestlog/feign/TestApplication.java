package io.github.requestlog.feign;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(scanBasePackages = "io.github.requestlog")
@EnableFeignClients
public class TestApplication {
}
