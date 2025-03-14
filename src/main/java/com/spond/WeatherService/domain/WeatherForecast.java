package com.spond.WeatherService.domain;

import java.time.LocalDateTime;

public record WeatherForecast(double temperature, double windSpeed, LocalDateTime lastUpdated) {
}
