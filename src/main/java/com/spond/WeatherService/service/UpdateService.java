package com.spond.WeatherService.service;

import com.spond.WeatherService.config.QueueConfig;
import com.spond.WeatherService.dto.LocationDTO;
import com.spond.WeatherService.dto.WeatherRequestDTO;
import com.spond.WeatherService.dto.WeatherResponseDTO;
import com.spond.WeatherService.entity.WeatherForecast;
import com.spond.WeatherService.repository.WeatherForecastRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class UpdateService {

    private final WeatherForecastRepository weatherForecastRepository;
    private final RabbitTemplate rabbitTemplate;

    //todo: fixed rate could be set by a property to control DB polling rate
    @Scheduled(fixedRate = 20 * 1000)
    public void sendForUpdates() {
        List<WeatherForecast> expiredWeatherForecast = weatherForecastRepository.findExpiredWeatherForecast();
        //todo: mocking, to test the rate limit
        expiredWeatherForecast = new ArrayList<>();
        for(int i=0; i< 40; i++) {
           expiredWeatherForecast.add(weatherForecast());
        }

//        expiredWeatherForecast = List.of(
//                weatherForecast(), weatherForecast(), weatherForecast(), weatherForecast(), weatherForecast());

        expiredWeatherForecast.forEach(wf -> rabbitTemplate.convertAndSend(QueueConfig.WEATHER_REQUEST_QUEUE,
                WeatherRequestDTO.builder()
                        .uuid(wf.getUuid())
                        .locationDTO(LocationDTO.builder().latitude(wf.getLatitude()).longitude(wf.getLongitude()).build())
                        .forecastTime(wf.getForecastTime())
                        .build()));
    }
    //todo just for local test run, can be removed
    private static WeatherForecast weatherForecast() {
       return new WeatherForecast(UUID.randomUUID().toString(), 60.0, 50.0, 0, 0, LocalDateTime.now(), LocalDateTime.now());
    }

    //todo: reading in batch from the queue in order to limit the amount of connections to the DB
    @Transactional
    @RabbitListener(queues = QueueConfig.WEATHER_RESPONSE_QUEUE, containerFactory = "batchContainerFactory")
    public void updateForecasts(List<WeatherResponseDTO> weatherResponseDTOS) {
        List<WeatherForecast> forecastList = weatherResponseDTOS.stream()
                .filter(dto -> {
                    if (dto.isHasError()) {
                        log.info(dto.getErrorMessage());
                        return false; //do not save failed entry
                    }
                    return true;
                })
                .map(this::mapDtoToEntity)
                .toList();

        //todo: not save all, but "update all", implement query in the repository
        weatherForecastRepository.saveAll(forecastList);
    }

    private WeatherForecast mapDtoToEntity(WeatherResponseDTO dto) {
        //todo: map fields
        return new WeatherForecast(
                dto.getUuid(),
                dto.getLocationDTO().getLatitude(),
                dto.getLocationDTO().getLongitude(),
                dto.getTemperature(),
                dto.getWindSpeed(),
                dto.getForecastTime(),
                //todo: based on forecast time, more distant forecasts (e.g. in 5 days) should get greater expiry e.g. 6 hours
                //todo: 1) to limit amount of requests to the API, 2) there is no weather forecast in the api anyway (based on a sample response)
                LocalDateTime.now().plusHours(2)
        );
    }
}
