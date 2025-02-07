package com.aniEx.geoMapAddressTool.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class GeoAddressController {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org";
    private final WebClient webClient;

    public GeoAddressController(WebClient.Builder webclientBuilder) {
        this.webClient = webclientBuilder.baseUrl(NOMINATIM_URL).build();
    }

    @CrossOrigin(origins = "http://localhost:63342")
    @GetMapping("/geocode")
    public Flux<Map<String, Object>> geocodePostcodes(@RequestParam List<String> postcodes) {
        return Flux.fromIterable(postcodes)
                .flatMap(postcode->fetchCoordinates(postcode)
                .map(response -> {
                    if (!response.isEmpty()) {
                        Map<String, Object> location = new HashMap<>();
                        location.put("postcode", postcode);
                        location.put("lat", response.getFirst().get("lat").asText());
                        location.put("lon", response.getFirst().get("lon").asText());
                        return location;
                    }
                    return null;}))
                .filter(Objects::nonNull);

    }

    private Mono<List<JsonNode>> fetchCoordinates(String postcode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search")
                        .queryParam("format", "json")
                        .queryParam("countrycodes", "US")
                        .queryParam("q", postcode)
                        .build())
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .collectList();
    }
}