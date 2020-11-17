package com.azanda.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;

@EnableJpaAuditing
@EnableFeignClients
@EnableCircuitBreaker
@EnableEurekaClient
@SpringBootApplication
public class DistributionApplication {
    // 这个是ribbon下的一个方法 通过RestTemplate去调用其他的微服务
    @Bean
    @LoadBalanced
    RestTemplate restTemplate()
    {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(DistributionApplication.class,args);
    }
}
