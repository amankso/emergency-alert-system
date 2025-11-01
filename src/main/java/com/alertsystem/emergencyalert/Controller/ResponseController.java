package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.Service.ResponseService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/respond")
@RequiredArgsConstructor
public class ResponseController {

    private final ResponseService responseService;


    //The contacted person submits a response message for a specific alert.
    @PostMapping("/{alertId}")
    public ResponseEntity<?> submitResponse(
            @PathVariable Long alertId,
            @RequestParam @NotBlank String responderPhone,
            @RequestParam @NotBlank String message
    ) {
        responseService.submitResponse(alertId, responderPhone, message);
        return ResponseEntity.ok("Response received successfully.");
    }


    //Fetch all responses for a given alert (visible to the alert creator).
    @GetMapping("/{alertId}")
    public ResponseEntity<?> getResponses(@PathVariable Long alertId) {
        return ResponseEntity.ok(responseService.getResponsesByAlert(alertId));
    }
}
