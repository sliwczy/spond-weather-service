package com.spond.WeatherService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spond.WeatherService.config.QueueConfig;
import com.spond.WeatherService.dto.WeatherForecastDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;
    private final WeatherResponseMappingService weatherMappingService;
    private final Semaphore semaphore = new Semaphore(20);

    private static final String API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact";
    public static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT_VALUE = "SpondWeatherService github.com/sliwczy/spond-weather-service";

    @RabbitListener(queues = QueueConfig.WEATHER_REQUEST_QUEUE)
    public void getWeatherInfo(WeatherForecastDTO dto) {
        var url = getUrl(dto.getLocation().getLatitude(), dto.getLocation().getLongitude());

        webClient.get().uri(url)
                //todo: according to met.no ToS pt.1 : "Identify yourself";
                .header(USER_AGENT_HEADER, USER_AGENT_VALUE)
                .retrieve().toEntity(String.class)
                .subscribe(
                        (response) -> handleResponse(response, dto),
                        (error) -> handleError(error, dto)
                );
    }

    private String getUrl(double latitude, double longitude) {
        return new StringBuilder().append(API_URL).append("?")
                .append("lat=").append(latitude)
                .append("&lon=").append(longitude).toString();
    }

    private void handleError(Throwable error, WeatherForecastDTO dto) {
        //todo: we let the client decide what to do in case e.g. Weather API is down. Client might decide to pause or stop sending messages
        log.info("error occurred: {}", error.getMessage());
        WeatherForecastDTO errorDto = WeatherForecastDTO.builder()
                .uuid(dto.getUuid())
                .hasError(true)
                .errorMessage(error.getMessage()).build();
        rabbitTemplate.convertAndSend(QueueConfig.WEATHER_RESPONSE_QUEUE, errorDto);
    }

    protected void handleResponse(ResponseEntity<String> response, WeatherForecastDTO dto) {
        try {
            WeatherForecastDTO updatedDto = weatherMappingService.jsonToWeatherObj(response.getBody(), dto);
            rabbitTemplate.convertAndSend(QueueConfig.WEATHER_RESPONSE_QUEUE, updatedDto);
        } catch (JsonProcessingException e) {
            handleError(e, dto);
        }
    }

    @Scheduled(fixedRate = 1000)
    private void releasePermits() {
        semaphore.release(20);
    }
}
