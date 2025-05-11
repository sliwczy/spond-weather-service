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
@JsonDeserialize(using = LocationDTO.LocationDeserializer.class)
public class LocationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 323176784185610336L;
    final double latitude;
    final double longitude;

    protected static class LocationDeserializer extends JsonDeserializer<LocationDTO> {

        @Override
        public LocationDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            ArrayNode coordinates = (ArrayNode) node.get("coordinates");

            double longitude = coordinates.get(0).asDouble();
            double latitude = coordinates.get(1).asDouble();

            return new LocationDTO(latitude, longitude);
        }
    }
}
