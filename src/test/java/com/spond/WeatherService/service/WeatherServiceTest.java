package com.spond.WeatherService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spond.WeatherService.domain.WeatherForecast;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @InjectMocks
    WeatherService weatherService;

    @Mock
    RestTemplate restTemplate;

    @Mock
    WeatherResponseMappingService weatherResponseMappingService;

    @Test
    public void testResponseOK() throws JsonProcessingException {
        //given
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body("ok"));
        when(weatherResponseMappingService.jsonToWeatherObj(any(String.class), any(LocalDateTime.class)))
                .thenReturn(
                        Optional.of(
                                new WeatherForecast(10, 1.5, LocalDateTime.now())));
        //when
        var response = weatherService.getWeatherInfo(0.5000, 0.500, LocalDateTime.now());
        //then
        assertTrue(response.isPresent());
    }


    @Test
    public void testResponseErrorCode() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.internalServerError().body("error"));

        assertThrows(HttpServerErrorException.class,
                () -> weatherService.getWeatherInfo(0.5000, 0.500, LocalDateTime.now()));

    }

    //todo: if there's ever any handling of JsonProcessingException in the WeatherService, this test will make sense
    //todo: for now it looks like we're testing mockito; added it only to show how moving mapping logic to separate service
    //todo: simplifies testing (no need to craft response to match format, mapping is decoupled)
    @Test
    public void testMalformedResponse() throws JsonProcessingException {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body("malformed"));
        when(weatherResponseMappingService.jsonToWeatherObj(any(String.class), any(LocalDateTime.class))).thenThrow(
                JsonProcessingException.class
        );

        assertThrows(JsonProcessingException.class, () -> weatherService.getWeatherInfo(0.5000, 0.500, LocalDateTime.now()));
    }
}