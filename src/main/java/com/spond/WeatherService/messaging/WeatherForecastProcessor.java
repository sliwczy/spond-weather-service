package com.spond.WeatherService.messaging;

import com.spond.WeatherService.config.QueueConfig;
import com.spond.WeatherService.dto.MetWeatherResponseDTO;
import com.spond.WeatherService.dto.WeatherRequestDTO;
import com.spond.WeatherService.dto.WeatherResponseDTO;
import com.spond.WeatherService.exception.NoForecastException;
import com.spond.WeatherService.client.MetWeatherApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

@RequiredArgsConstructor
@Slf4j
@Component
public class WeatherForecastProcessor {

    private final MetWeatherApiClient metWeatherApiClient;
    private final RabbitTemplate rabbitTemplate;
    private final Semaphore semaphore = new Semaphore(3);

    @Scheduled(fixedRate = 5000)
    private void releasePermits() {
        semaphore.release(3);
    }

    @RabbitListener(queues = QueueConfig.WEATHER_REQUEST_QUEUE)
    public void getMetWeatherApiClient(WeatherRequestDTO requestDTO) throws InterruptedException {
        semaphore.acquire();
        metWeatherApiClient.getWeatherInfo(requestDTO)
                .subscribe(
                        (response) -> handleResponse(response, requestDTO),
                        (error) -> handleError(error, requestDTO)
                );
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

    private Optional<MetWeatherResponseDTO.MetForecast> findForecastInTimeSeries(List<MetWeatherResponseDTO.MetForecast> timeSeries, LocalDateTime requestedForecastTime) {
        return timeSeries.stream()
                .filter(entry -> entry.getForecastTime().equals(requestedForecastTime.truncatedTo(ChronoUnit.HOURS)))
                .findFirst();
    }
}
