package com.spond.WeatherService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class WeatherRequestDTO implements Serializable {
    private final String uuid;
    @NonNull
    private final LocationDTO locationDTO;
    @NonNull
    final LocalDateTime forecastTime;
}
