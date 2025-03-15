package com.spond.WeatherService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherForecast {

    @Id
    private String uuid;

    private double longitude;
    private double latitude;
    private double temperature;
    private double windSpeed;
    private LocalDateTime forecastTime;
    private LocalDateTime expiresAt;
}
