package com.spond.WeatherService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestConfiguration {


    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }
}
