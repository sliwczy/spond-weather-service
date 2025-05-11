package com.spond.WeatherService.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

class MetWeatherResponseDeserializerTest {

    //writing these tests because wrote custom deserializer for that class
    @Test
    public void testMetMapping() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        MetWeatherResponseDTO metWeatherResponseDTO = objectMapper.readValue(getJsonSample("sample_response.json"), MetWeatherResponseDTO.class);

        metWeatherResponseDTO.getTimeSeries().forEach(ts -> System.out.println(ts.getForecastTime()));
    }

    @Test
    public void testMalformedResponse() {
        ObjectMapper objectMapper = new ObjectMapper();
        assertThrows(JsonProcessingException.class,
                () -> objectMapper.readValue("malformed", MetWeatherResponseDTO.class));
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