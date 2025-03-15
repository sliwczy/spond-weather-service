package com.spond.WeatherService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spond.WeatherService.config.QueueConfig;
import com.spond.WeatherService.dto.Location;
import com.spond.WeatherService.dto.WeatherForecastDTO;
import com.spond.WeatherService.entity.WeatherForecast;
import com.spond.WeatherService.repository.WeatherForecastRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UpdateService {

    private final WeatherForecastRepository weatherForecastRepository;
    private final WeatherService weatherService;
    private final RabbitTemplate rabbitTemplate;

    //todo: fixed rate could be set by a property to control DB polling rate
    @Scheduled(fixedRate = 20 * 1000)
    public void sendForUpdates() {
        List<WeatherForecast> expiredWeatherForecast = weatherForecastRepository.findExpiredWeatherForecast();

        expiredWeatherForecast.forEach(wf -> rabbitTemplate.convertAndSend(QueueConfig.WEATHER_REQUEST_QUEUE,
                WeatherForecastDTO.builder()
                        .uuid(wf.getUuid())
                        .location(Location.builder().latitude(wf.getLatitude()).longitude(wf.getLongitude()).build())
                        .forecastTime(wf.getForecastTime())
                        .build()));
    }

    //todo: reading in batch from the queue in order to limit the amount of connections to the DB
    @Scheduled(fixedRate = 20 * 1000)
    @RabbitListener(queues = QueueConfig.WEATHER_RESPONSE_QUEUE, containerFactory = "batchContainerFactory")
    public void updateForecasts(List<WeatherForecastDTO> weatherForecastDTOS) {
        List<WeatherForecast> forecastList = weatherForecastDTOS.stream()
                .filter(dto -> {
                    if (dto.getErrorMessage().isPresent()) {
                        log.info(dto.getErrorMessage().get());
                        return false;
                    }
                    return true;
                })
                .map(this::mapDtoToEntity)
                .toList();

        //todo: not save all, but "update all", implement query in the repository
        weatherForecastRepository.saveAll(forecastList);
    }

    private WeatherForecast mapDtoToEntity(WeatherForecastDTO dto) {
        //todo: map fields
        return new WeatherForecast(
                dto.getUuid(),
                dto.getLocation().getLatitude(),
                dto.getLocation().getLongitude(),
                dto.getTemperature(),
                dto.getWindSpeed(),
                dto.getForecastTime(),
                //todo: based on forecast time, more distant forecasts (e.g. in 5 days) should get greater expiry e.g. 6 hours
                //todo: 1) to limit amount of requests to the API, 2) there is no weather forecast in the api anyway (based on a sample response)
                LocalDateTime.now().plusHours(2)
        );
    }
}
