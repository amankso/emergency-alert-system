package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.AlertDTO;
import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import com.alertsystem.emergencyalert.Service.AlertService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // Create + send alert
    @PostMapping
    public ResponseEntity<?> sendAlert(
            @RequestHeader String sessionToken,
            @Valid @RequestBody AlertDTO alertDTO
    ) {
        try {
            AlertEntity alert = alertService.sendAlert(sessionToken, alertDTO);
            if (alert.getStatus() == AlertStatusEnum.FAILED) {
                return ResponseEntity.status(500)
                        .body(Map.of("success", false, "message", "Failed to send alert (SMS not delivered)", "alert", alert));
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Alert sent successfully", "alert", alert));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
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
