package com.spond.WeatherService.client;

import com.spond.WeatherService.dto.MetWeatherResponseDTO;
import com.spond.WeatherService.dto.WeatherRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetWeatherApiClient {

    private final WebClient webClient;

    private static final String API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact";
    public static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT_VALUE = "SpondWeatherService github.com/sliwczy/spond-weather-service";

    public Mono<ResponseEntity<MetWeatherResponseDTO>> getWeatherInfo(WeatherRequestDTO requestDTO) {
        log.info("acquired token to proceed with weather request");
        var url = getUrl(requestDTO.getLocationDTO().getLatitude(), requestDTO.getLocationDTO().getLongitude());

        log.info("sending request to {}", url);
        return webClient.get().uri(url)
                .header(USER_AGENT_HEADER, USER_AGENT_VALUE)//according to met.no ToS pt.1 : "Identify yourself";
                .retrieve()
                .toEntity(MetWeatherResponseDTO.class);
    }

    private String getUrl(double latitude, double longitude) {
        return new StringBuilder().append(API_URL).append("?")
                .append("lat=").append(latitude)
                .append("&lon=").append(longitude).toString();
    }

}
