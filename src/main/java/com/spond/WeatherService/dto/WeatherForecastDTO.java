package com.spond.WeatherService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@Data
@AllArgsConstructor
public class WeatherForecastDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4226275314805486112L;
    private final String uuid;
    @NonNull
    private final Location location;
    private double temperature;
    private double windSpeed;
    @NonNull
    final LocalDateTime forecastTime;
    private final boolean hasError;
    private final String errorMessage;
}
