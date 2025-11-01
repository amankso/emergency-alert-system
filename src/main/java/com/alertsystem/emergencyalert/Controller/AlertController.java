package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.AlertDTO;
import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Service.AlertService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // Create + send alert
    @PostMapping
    public ResponseEntity<AlertEntity> sendAlert(
            @RequestHeader String sessionToken,
            @Valid @RequestBody AlertDTO alertDTO
    ) throws JsonProcessingException {
        return ResponseEntity.ok(alertService.sendAlert(sessionToken, alertDTO));
    }

    // Get all alerts of a user
    @GetMapping
    public ResponseEntity<List<AlertEntity>> getUserAlerts(
            @RequestHeader String sessionToken
    ) {
        return ResponseEntity.ok(alertService.getUserAlerts(sessionToken));
    }

    // Get only active alerts
    @GetMapping("/active")
    public ResponseEntity<List<AlertEntity>> getActiveAlerts(
            @RequestHeader String sessionToken
    ) {
        return ResponseEntity.ok(alertService.getActiveAlerts(sessionToken));
    }
}
