package com.spond.WeatherService.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NoForecastException extends Exception {

    public NoForecastException(String message) {
        super(message);
    }
}
