package com.spond.WeatherService.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spond.WeatherService.dto.WeatherForecastDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

@Service
public class WeatherResponseMappingService {

    public WeatherForecastDTO jsonToWeatherObj(String responseJson, WeatherForecastDTO requestDto) throws JsonProcessingException {

        var timeSeries = new ObjectMapper().readTree(responseJson)
                .path("properties")
                .path("timeseries")
                .spliterator();
        Optional<JsonNode> optionalForecastRow = findForecastInTimeSeries(timeSeries, requestDto.getForecastTime());

        if (optionalForecastRow.isEmpty()) {
            throw new JsonParseException("no weather forecast for a given time!");
        }

        JsonNode forecastNode = optionalForecastRow.get().path("data").path("instant").path("details");
        var temperature = forecastNode.path("air_temperature");
        var windSpeed = forecastNode.path("wind_speed");

        if (temperature.isMissingNode() || windSpeed.isMissingNode()) {
            throw new JsonParseException("weather forecast information is missing in the response!");
        }
        return WeatherForecastDTO.builder()
                .uuid(requestDto.getUuid())
                .location(requestDto.getLocation())
                .forecastTime(requestDto.getForecastTime())
                .temperature(temperature.asDouble())
                .windSpeed(windSpeed.asDouble())
                .errorMessage(Optional.empty())
                .build();
    }

    //todo: later days contain less entries, write a code that will match the closest hour
    private Optional<JsonNode> findForecastInTimeSeries(Spliterator<JsonNode> timeSeries, LocalDateTime requestedForecastTime) {
        var truncatedTime = requestedForecastTime.truncatedTo(ChronoUnit.HOURS);

        return StreamSupport.stream(timeSeries, false)
                .filter(entry -> {
                    String timeStr = entry.path("time").asText();
                    LocalDateTime forecastTime = LocalDateTime.ofInstant(Instant.parse(timeStr), ZoneOffset.UTC);
                    return forecastTime.isEqual(truncatedTime);
                })
                .findFirst();
    }
}
