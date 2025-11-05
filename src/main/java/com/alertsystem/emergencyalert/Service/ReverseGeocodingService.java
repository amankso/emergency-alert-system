package com.alertsystem.emergencyalert.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Service
@Slf4j
public class ReverseGeocodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse";

    public String getRegionFromCoordinates(Double latitude, Double longitude) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("format", "json")
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            Map response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("address")) {
                Map address = (Map) response.get("address");
                String region = (String) address.getOrDefault("state_district",
                        address.getOrDefault("state", "Unknown"));
                return region;
            }
        } catch (Exception ex) {
            log.error("❌ Failed to reverse geocode: {}", ex.getMessage());
        }
        return "Unknown";
    }
}
