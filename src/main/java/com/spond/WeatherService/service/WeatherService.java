package com.spond.WeatherService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spond.WeatherService.domain.WeatherForecast;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;
    private final WeatherResponseMappingService weatherMappingService;

    private static final String API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact";

    public Optional<WeatherForecast> getWeatherInfo(double latitude, double longitude, LocalDateTime forecastDateTime) throws JsonProcessingException {
        //convert event time to the desired forecast
        //handle status for 404, 403, 500 etc
        //there still may be null response if there is no forecast for a given day, or some random message or malformed response with http 200

        var response = queryApi(latitude, longitude);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new HttpServerErrorException(response.getStatusCode());
        }

        return weatherMappingService.jsonToWeatherObj(response.getBody(), forecastDateTime);
    }

    private ResponseEntity<String> queryApi(double latitude, double longitude) {
        String url = new StringBuilder().append(API_URL).append("?")
                .append("lat=").append(latitude)
                .append("&lon=").append(longitude).toString();

        return restTemplate.getForEntity(url, String.class);
    }

}
