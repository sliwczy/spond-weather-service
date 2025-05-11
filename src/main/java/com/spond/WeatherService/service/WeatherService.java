package com.spond.WeatherService.service;

import com.spond.WeatherService.config.QueueConfig;
import com.spond.WeatherService.dto.MetWeatherResponseDTO;
import com.spond.WeatherService.dto.WeatherRequestDTO;
import com.spond.WeatherService.dto.WeatherResponseDTO;
import com.spond.WeatherService.exception.NoForecastException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;
    private final Semaphore semaphore = new Semaphore(3);

    private static final String API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact";
    public static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT_VALUE = "SpondWeatherService github.com/sliwczy/spond-weather-service";

    @RabbitListener(queues = QueueConfig.WEATHER_REQUEST_QUEUE)
    public void getWeatherInfo(WeatherRequestDTO requestDTO) throws InterruptedException {
        semaphore.acquire();
        log.info("acquired token to proceed with weather request");
        var url = getUrl(requestDTO.getLocationDTO().getLatitude(), requestDTO.getLocationDTO().getLongitude());

        log.info("sending request to {}", url);
        webClient.get().uri(url)
                .header(USER_AGENT_HEADER, USER_AGENT_VALUE)//according to met.no ToS pt.1 : "Identify yourself";
                .retrieve()
                .toEntity(MetWeatherResponseDTO.class)
                .subscribe(
                        (response) -> handleResponse(response, requestDTO),
                        (error) -> handleError(error, requestDTO)
                );
    }

    private String getUrl(double latitude, double longitude) {
        return new StringBuilder().append(API_URL).append("?")
                .append("lat=").append(latitude)
                .append("&lon=").append(longitude).toString();
    }

    private void handleError(Throwable error, WeatherRequestDTO originalRequestDTO) {
        log.info("error occurred: {}", error.getMessage());
        WeatherResponseDTO errorDto = WeatherResponseDTO.builder()
                .uuid(originalRequestDTO.getUuid())
                .hasError(true)
                .errorMessage(error.getMessage()).build();
        rabbitTemplate.convertAndSend(QueueConfig.WEATHER_RESPONSE_QUEUE, errorDto);
    }

    protected void handleResponse(ResponseEntity<MetWeatherResponseDTO> metResponse, WeatherRequestDTO originalRequestDTO) {
        if (!metResponse.hasBody()) {
            handleError(new NoForecastException("No response from the API."), originalRequestDTO);
        }

        Optional<MetWeatherResponseDTO.MetForecast> forecast = findForecastInTimeSeries(metResponse.getBody().getTimeSeries(), originalRequestDTO.getForecastTime());
        if (forecast.isEmpty()) {
            handleError(new NoForecastException("Service returned forecast but none was found for a given time."), originalRequestDTO);
        }

        WeatherResponseDTO responseDto = WeatherResponseDTO.builder()
                .uuid(originalRequestDTO.getUuid())
                .locationDTO(originalRequestDTO.getLocationDTO())
                .windSpeed(forecast.get().getWindSpeed())
                .temperature(forecast.get().getTemperature())
                .hasError(false)
                .build();
        log.info("updated weather: {}", responseDto);
        rabbitTemplate.convertAndSend(QueueConfig.WEATHER_RESPONSE_QUEUE, responseDto);
    }

    @Scheduled(fixedRate = 5000)
    private void releasePermits() {
        semaphore.release(3);
    }

    private Optional<MetWeatherResponseDTO.MetForecast> findForecastInTimeSeries(List<MetWeatherResponseDTO.MetForecast> timeSeries, LocalDateTime requestedForecastTime) {
        return timeSeries.stream()
                .filter(entry -> entry.getForecastTime().equals(requestedForecastTime))
                .findFirst();
    }
}
