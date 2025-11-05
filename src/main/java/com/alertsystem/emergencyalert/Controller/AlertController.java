package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.AlertDTO;
import com.alertsystem.emergencyalert.DTO.PoliceAlertDTO;
import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import com.alertsystem.emergencyalert.Repository.AlertRepository;
import com.alertsystem.emergencyalert.Service.AlertService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AlertController {

    private final AlertService alertService;
    private final AlertRepository alertRepository;

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

    @GetMapping("/export")
    public void exportAlertsCsv(
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String region,
            HttpServletResponse response
    ) throws IOException {

        // 1️⃣ Get filtered alerts
        Page<PoliceAlertDTO> alerts = alertService.getAlertsForPolice(
                mobileNumber, status, from, to, 0, region // 0 = all for export
        );

        // 2️⃣ Set response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"alerts.csv\"");

        // 3️⃣ Write CSV
        PrintWriter writer = response.getWriter();
        writer.println("Name,Mobile,Region,Status,Custom Message,Sent To,Map URL,Timestamp");

        for (PoliceAlertDTO alert : alerts) {
            String sentTo = alert.getSentTo().stream()
                    .map(c -> c.get("name") + "(" + c.get("mobileNumber") + ")")
                    .collect(Collectors.joining("; "));

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    alert.getAlerteeName(),
                    alert.getMobileNumber(),
                    alert.getRegion(),
                    alert.getStatus(),
                    alert.getCustomMessage() != null ? alert.getCustomMessage() : "",
                    sentTo,
                    alert.getMapUrl(),
                    alert.getAlertTimestamp()
            );
        }

        writer.flush();
    }


    @GetMapping("/alerts/stats")
    public ResponseEntity<?> getDashboardStats(@RequestHeader("sessionToken") String sessionToken) {
        Map<String, Long> stats = alertService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // DELETE single alert by ID
    @DeleteMapping("/{alertId}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long alertId) {
        return alertRepository.findById(alertId)
                .map(alert -> {
                    alertRepository.deleteById(alertId);
                    return ResponseEntity.ok(Map.of("message", "Alert deleted successfully"));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Alert not found")));
    }

    // PATCH alert -> mark as resolved
    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable Long alertId) {
        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(AlertStatusEnum.RESOLVED);
                    alertRepository.save(alert);
                    return ResponseEntity.ok(Map.of("message", "Alert marked as resolved"));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Alert not found")));
    }
}



