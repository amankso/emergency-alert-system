package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.Entity.PoliceStationEntity;
import com.alertsystem.emergencyalert.Repository.PoliceStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoliceStationService {

    private final PoliceStationRepository repository;

    // 🔹 Return all stations within 10 km (sorted by distance)
    public List<PoliceStationEntity> findNearestStations(double userLat, double userLng) {
        List<PoliceStationEntity> allStations = repository.findAll();
        return allStations.stream()
                .map(station -> new Object[]{
                        station,
                        distance(userLat, userLng,
                                station.getLatitude().doubleValue(),
                                station.getLongitude().doubleValue())
                })
                .filter(arr -> (double) arr[1] <= 11.0)
                .sorted((a, b) -> Double.compare((double) a[1], (double) b[1]))
                .map(arr -> (PoliceStationEntity) arr[0])
                .collect(Collectors.toList());
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
