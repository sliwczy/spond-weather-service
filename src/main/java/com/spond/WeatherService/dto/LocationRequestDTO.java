package com.spond.WeatherService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class LocationRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 323373734135610336L;
    final double latitude;
    final double longitude;
}
