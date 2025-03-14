package com.spond.WeatherService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spond.WeatherService.dto.WeatherForecastDTO;
import com.spond.WeatherService.entity.WeatherForecast;
import com.spond.WeatherService.repository.WeatherForecastRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UpdateService {

    private WeatherForecastRepository weatherForecastRepository;

    private WeatherService weatherService;

    //todo: fixed rate could be set by a property to control DB polling rate
    @Scheduled(fixedRate = 20 * 1000)
    public void update() {
        //todo: temporary mocking
//        List<WeatherForecast> expiredWeatherForecast = weatherForecastRepository.findExpiredWeatherForecast();
        var expiredWeatherForecast = List.of(new WeatherForecast("asdf1234", 60.0, 60.0, LocalDateTime.now(), LocalDateTime.now()));

        expiredWeatherForecast.forEach(wf -> {
            try {
                Optional<WeatherForecastDTO> weatherInfo = weatherService.getWeatherInfo(wf.getLatitude(), wf.getLongitude(), wf.getForecastTime());
                log.info(weatherInfo.map(Object::toString).orElse("No weather forecast for given parameters"));
            } catch (JsonProcessingException e) {
                //todo: adding this temporary handling
                log.info("fetching weather failed for long:{} lat:{} forecastTime:{}", wf.getLatitude(), wf.getLongitude(), wf.getForecastTime());
            }
        });
    }
}
