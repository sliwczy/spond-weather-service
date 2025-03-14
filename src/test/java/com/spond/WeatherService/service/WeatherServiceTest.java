package com.spond.WeatherService.service;

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

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @InjectMocks
    WeatherService weatherService;

    @Mock
    RestTemplate restTemplate;

    @Test
    public void testResponseOK() {
        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(String.class)))
                .thenReturn(ResponseEntity.ok().body("weather forecast"));

        Optional<String> response = weatherService.getWeatherInfo(0.5000, 0.500, LocalDateTime.now());
        assertTrue(response.isPresent());
    }


    @Test
    public void testResponseErrorCode() {
        var errorResponse = ResponseEntity.internalServerError().body("error");

        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(errorResponse);
        assertThrows(HttpServerErrorException.class,
                () -> weatherService.getWeatherInfo(0.5000, 0.500, LocalDateTime.now()));

    }
}