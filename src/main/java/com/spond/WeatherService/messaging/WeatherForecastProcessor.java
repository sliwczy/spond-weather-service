package com.spond.WeatherService.messaging;

import com.spond.WeatherService.client.MetWeatherApiClient;
import com.spond.WeatherService.config.QueueConfig;
import com.spond.WeatherService.dto.LocationResponseDTO;
import com.spond.WeatherService.dto.MetWeatherResponseDTO;
import com.spond.WeatherService.dto.WeatherRequestDTO;
import com.spond.WeatherService.dto.WeatherResponseDTO;
import com.spond.WeatherService.exception.NoForecastException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${queue.weather.rq.rate}")
    private int maxRequests;

    private final MetWeatherApiClient metWeatherApiClient;
    private final RabbitTemplate rabbitTemplate;
    private final Semaphore semaphore = new Semaphore(maxRequests);

    @Scheduled(fixedRate = 5000)
    private void releasePermits() {
        semaphore.release(maxRequests);
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
                .locationDTO(
                        LocationResponseDTO.builder()
                                .latitude(originalRequestDTO.getLocationRequestDTO().getLatitude())
                                .longitude(originalRequestDTO.getLocationRequestDTO().getLongitude())
                                .build()
                )
                .forecastTime(originalRequestDTO.getForecastTime())
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
                .locationDTO(
                        LocationResponseDTO.builder()
                        .latitude(originalRequestDTO.getLocationRequestDTO().getLatitude())
                        .longitude(originalRequestDTO.getLocationRequestDTO().getLongitude())
                        .build()
                )
                .forecastTime(originalRequestDTO.getForecastTime())
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
