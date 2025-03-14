package com.spond.WeatherService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spond.WeatherService.service.WeatherService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class WeatherServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(WeatherService weatherService) {
		return args -> {
			weatherService.getWeatherInfo(60.000, 60.000, LocalDateTime.now());
		};
	}
}
