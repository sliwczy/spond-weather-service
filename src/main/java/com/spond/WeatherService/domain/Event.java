package com.spond.WeatherService.domain;

public record Event(String uuid, String name, Location location, WeatherForecast weatherForecast) {}
