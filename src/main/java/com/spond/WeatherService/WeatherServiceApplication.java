package com.spond.WeatherService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spond.WeatherService.domain.WeatherForecast;
import com.spond.WeatherService.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Optional;
@Slf4j
@SpringBootApplication
public class WeatherServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(WeatherService weatherService) {
		return args -> {
			var optionalWeather = weatherService.getWeatherInfo(60.000, 60.000, LocalDateTime.now());
			log.info(optionalWeather.map(Object::toString).orElse("No weather forecast for given parameters"));
		};
	}
}
