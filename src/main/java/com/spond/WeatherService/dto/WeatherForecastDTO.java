package com.spond.WeatherService.dto;

import java.time.LocalDateTime;

public record WeatherForecastDTO(double temperature, double windSpeed, LocalDateTime forecastTime) {
}
