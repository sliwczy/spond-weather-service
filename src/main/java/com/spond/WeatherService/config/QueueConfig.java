package com.spond.WeatherService.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class QueueConfig {

    @Value("${queue.weather.rs.prefetch.count}")
    private int weatherResponsePrefetchCount;
    public static final String WEATHER_RESPONSE_QUEUE = "weather_response_queue";
    public static final String WEATHER_REQUEST_QUEUE = "weather_queue";

    @Bean
    public Queue requestQueue() {
            return new Queue(WEATHER_REQUEST_QUEUE, false);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(WEATHER_RESPONSE_QUEUE, false);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, SimpleMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);  // Explicitly setting the converter
        return template;
    }

    @Bean
    public SimpleMessageConverter messageConverter() {
        var converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of(
                "com.spond.WeatherService.dto.*",
                "java.util.*",
                "java.time.*"
        ));
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory batchContainerFactory(ConnectionFactory connectionFactory, SimpleMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setMessageConverter(messageConverter);
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(weatherResponsePrefetchCount);
        return factory;
    }
}
