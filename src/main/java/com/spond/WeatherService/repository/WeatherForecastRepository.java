package com.spond.WeatherService.repository;

import com.spond.WeatherService.entity.WeatherForecast;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {
    // todo: the way data is updated and accessed in many ways depends on the existing database setup for Event entity
    // todo: here it's more relational-db leaning, but can be changed based on needs
    // todo: one thing is certain - there is a need of some sort of caching of the WeatherForecast so that users don't
    // todo: query that WeatherService each time they open the Event; Instead they will get prepopulated forecast for that specific location

    //todo: Weather service will query for expired weather entries in bulk and update them
    @Query("SELECT wf FROM WeatherForecast wf WHERE wf.expiresAt < current_timestamp")
    List<WeatherForecast> findExpiredWeatherForecast();

    @Override
    <S extends WeatherForecast> S save(S entity);
}
