package com.spond.WeatherService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@Data
@AllArgsConstructor
public class WeatherForecastDTO {
    private final String uuid;
    @NonNull
    private final Location location;
    private double temperature;
    private double windSpeed;
    @NonNull
    final LocalDateTime forecastTime;
    @NonNull
    private final Optional<String> errorMessage;
}
