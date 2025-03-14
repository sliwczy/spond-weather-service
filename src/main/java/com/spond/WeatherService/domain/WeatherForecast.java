package com.spond.WeatherService.domain;

import java.time.LocalDateTime;

public record WeatherForecast(int temperature, double windSpeed, LocalDateTime lastUpdated) {
}
