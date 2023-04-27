package com.amyojiakor.AccountMicroService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class ApiConfig {
    @Value("${myapp.api.base-url.user-service}")
    private String userServiceBaseUrl;

    @Value("${myapp.api.base-url.transaction-service}")
    private String transactionsServiceBaseUrl;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getUserServiceBaseUrl() {
        return userServiceBaseUrl;
    }

    private String getTransactionsServiceBaseUrl() {
        return transactionsServiceBaseUrl;
    }
}
