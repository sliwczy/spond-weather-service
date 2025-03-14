package com.spond.WeatherService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spond.WeatherService.dto.WeatherForecastDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeatherResponseMappingServiceTest {

    @InjectMocks
    WeatherResponseMappingService mappingService;

    //todo: because I'm accessing nodes in DOM style rather than parsing for pojos, I thought it's good to check
    // if node matching works
    @Test
    public void testResponseInExpectedFormat() throws JsonProcessingException {
        Optional<WeatherForecastDTO> weatherForecast = mappingService.jsonToWeatherObj(
                getJsonSample("sample_response.json"),
                LocalDateTime.parse("2025-03-14T12:00:00")
        );
        assertTrue(weatherForecast.isPresent());
        assertEquals(4.1, weatherForecast.get().temperature());
        assertEquals(4.3, weatherForecast.get().windSpeed());
    }

    @Test
    public void testMalformedResponse() {
        assertThrows(JsonProcessingException.class,
                () -> mappingService.jsonToWeatherObj("malformed", LocalDateTime.now()));
    }

    private String getJsonSample(String fileName) {

        var sb = new StringBuilder();

        var fileInputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("error reading sample file!");
            System.out.println(e.getMessage());
        }

        return sb.toString();
    }

}