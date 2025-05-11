package com.spond.WeatherService.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@JsonDeserialize(using = MetWeatherResponseDTO.MetWeatherDeserializer.class)
public class MetWeatherResponseDTO implements Serializable {

    private List<MetForecast> timeSeries;

    @Builder
    @Data
    @AllArgsConstructor
    public static class MetForecast implements Serializable {
        private LocalDateTime forecastTime;
        private double temperature;
        private double windSpeed;
    }

    protected static class MetWeatherDeserializer extends JsonDeserializer<MetWeatherResponseDTO> {

        @Override
        public MetWeatherResponseDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode root = jp.getCodec().readTree(jp);

            List<MetForecast> forecasts = new ArrayList<>();

            JsonNode timeseries = root.path("properties").path("timeseries");
            for (JsonNode entry : timeseries) {
                String timeStr = entry.path("time").asText();
                LocalDateTime forecastTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME);

                JsonNode details = entry.path("data").path("instant").path("details");
                double temp = details.path("air_temperature").asDouble();
                double wind = details.path("wind_speed").asDouble();

                forecasts.add(new MetForecast(forecastTime, temp, wind));
            }

            return new MetWeatherResponseDTO(forecasts);
        }
    }
}
