package com.github.requestlog.feign;

import com.github.requestlog.core.repository.IRequestLogRepository;
import com.github.requestlog.core.repository.impl.InMemoryRequestLogRepository;
import com.github.requestlog.core.support.SupplierChain;
import com.github.requestlog.feign.clients.TestFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;


@Configuration
public class FeignTestConfiguration {


    @Bean
    public IRequestLogRepository requestLogRepository() {
        return new InMemoryRequestLogRepository();
    }


    /**
     * Provides a ServiceInstanceListSupplier bean for Feign clients in test scenarios to simulate service discovery.
     * This bean is used in test contexts to supply a single simulated ServiceInstance for a specified service.
     */
    @Bean
    @Lazy
    public ServiceInstanceListSupplier localServiceInstanceListSupplier(@Value("${local.server.port:#{null}}") Integer localServerPort,
                                                                        @Value("${server.port:#{null}}") Integer serverPort) {

        return new ServiceInstanceListSupplier() {
            @Override
            public String getServiceId() {
                return TestFeignClient.SERVICE_ID;
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                return Flux.just(Collections.singletonList(new DefaultServiceInstance(getServiceId(), getServiceId(), "localhost", SupplierChain.of(localServerPort).or(serverPort).get(), false)));
            }
        };
    }

}
