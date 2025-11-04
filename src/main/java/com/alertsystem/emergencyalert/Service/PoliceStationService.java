package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import com.alertsystem.emergencyalert.Repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PoliceStationService {

    private final AlertRepository alertRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value( "${geoapify.api.key}")
    private String GEOAPIFY_API_KEY;

    // Fetch nearby police stations from Geoapify
    public List<String> fetchNearbyStations(double lat, double lon) {
        List<String> stations = new ArrayList<>();
        String url = String.format(
                "https://nominatim.openstreetmap.org/search?format=json&q=police+station+near+%f,%f&limit=5",
                lat, lon
        );

        try {
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> body = response.getBody();

            if (body != null) {
                for (Map<String, Object> place : body) {
                    String displayName = (String) place.get("display_name");
                    if (displayName != null) {
                        stations.add(displayName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stations;
    }


    // Update alert with fetched police stations
    public AlertEntity updateAlertWithNearbyStations(Long alertId, double lat, double lon) {
        AlertEntity alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        List<String> stations = fetchNearbyStations(lat, lon);
        alert.setNearbyPoliceStations(String.join(",", stations));
        return alertRepository.save(alert);
    }

    // Get all alerts by status (for police dashboard)
    public List<AlertEntity> getAlertsByStatus(AlertStatusEnum status) {
        return alertRepository.findAllByStatus(status);
    }

    // Search by police station name keyword
    public List<AlertEntity> searchAlertsByStationKeyword(String keyword) {
        return alertRepository.searchByNearbyStationName(keyword);
    }
}
