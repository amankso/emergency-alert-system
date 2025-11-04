package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import com.alertsystem.emergencyalert.Service.PoliceStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/police")
@RequiredArgsConstructor
public class PoliceStationController {

    private final PoliceStationService policeStationService;

    //  Fetch all alerts by status (e.g. PENDING, RESOLVED)
    @GetMapping("/alerts/{status}")
    public ResponseEntity<List<AlertEntity>> getAlertsByStatus(@PathVariable("status") AlertStatusEnum status) {
        return ResponseEntity.ok(policeStationService.getAlertsByStatus(status));
    }

    //  Update alert with nearest police stations
    @PostMapping("/update-stations/{alertId}")
    public ResponseEntity<AlertEntity> updateNearbyStations(
            @PathVariable Long alertId,
            @RequestParam double lat,
            @RequestParam double lon) {
        return ResponseEntity.ok(policeStationService.updateAlertWithNearbyStations(alertId, lat, lon));
    }

    // Search alerts by police station keyword
    @GetMapping("/alerts/search")
    public ResponseEntity<List<AlertEntity>> searchAlerts(@RequestParam String keyword) {
        return ResponseEntity.ok(policeStationService.searchAlertsByStationKeyword(keyword));
    }
}
