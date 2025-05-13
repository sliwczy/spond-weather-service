package com.spond.WeatherService.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class LocationResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 323176784185610336L;
    final double latitude;
    final double longitude;
}
