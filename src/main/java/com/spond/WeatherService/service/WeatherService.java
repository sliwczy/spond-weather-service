package com.spond.WeatherService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    public void queryApi() {

        var apiEndpointUrl = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=60.0&lon=60.0";
        String response = restTemplate.getForObject(apiEndpointUrl, String.class);
        log.info(response);
    }
}
