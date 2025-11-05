package com.alertsystem.emergencyalert.Util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Component
public class LocationResolver {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getRegionFromCoordinates(Double lat, Double lon) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://nominatim.openstreetmap.org/reverse")
                    .queryParam("format", "json")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("addressdetails", "1")
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("address"))
                return "Unknown";

            Map<String, Object> address = (Map<String, Object>) response.get("address");

            String state = (String) address.get("state");
            String district = (String) address.getOrDefault("state_district", address.get("county"));

            if (district != null && state != null)
                return district + ", " + state;
            else if (state != null)
                return state;
            else return "Unknown";

        } catch (Exception e) {
            return "Unknown";
        }
    }
}
