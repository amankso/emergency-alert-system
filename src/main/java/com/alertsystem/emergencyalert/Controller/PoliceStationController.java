package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.PoliceAlertDTO;
import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import com.alertsystem.emergencyalert.Entity.UserRole;
import com.alertsystem.emergencyalert.Service.AlertService;
import com.alertsystem.emergencyalert.Service.AuthService;
import com.alertsystem.emergencyalert.Service.PoliceStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/police")
@RequiredArgsConstructor
public class PoliceStationController {

    private final PoliceStationService policeStationService;
    private final AuthService authService;
    private final AlertService alertService;

    @PostMapping("/nearest")
    public ResponseEntity<?> getNearestStations(
            @RequestHeader("sessionToken") String sessionToken,
            @RequestBody Map<String, Double> coords) {

        UserEntity user = authService.getUserBySessionToken(sessionToken);

        Double lat = coords.get("latitude");
        Double lng = coords.get("longitude");

        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body("Latitude and longitude required");
        }

        var nearbyStations = policeStationService.findNearestStations(lat, lng);

        if (nearbyStations.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No nearby stations within 10 km"));
        }

        // ✅ Include latitude and longitude in the response
        var response = nearbyStations.stream().map(station -> Map.of(
                "stationName", station.getStationName(),
                "contactNumber", station.getContactNumber(),
                "latitude", station.getLatitude(),
                "longitude", station.getLongitude(),
                "mapUrl", "https://www.google.com/maps?q=" +
                        station.getLatitude() + "," + station.getLongitude()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }



    @GetMapping("/alerts")
    public ResponseEntity<Page<PoliceAlertDTO>> getFilteredAlerts(
            @RequestHeader("sessionToken") String sessionToken,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String region

    ) {
        UserEntity user = authService.getUserBySessionToken(sessionToken);

        if(user.getRole() != UserRole.POLICE_OFFICIAL){
            return ResponseEntity.status(403).build();
        }

        Page<PoliceAlertDTO> alerts = alertService.getAlertsForPolice(mobileNumber, status, from, to, page,region);
        return ResponseEntity.ok(alerts);
    }





}
