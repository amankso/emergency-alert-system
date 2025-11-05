package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.UserDTO;
import com.alertsystem.emergencyalert.Service.AlertService;
import com.alertsystem.emergencyalert.Service.AuthService;
import com.alertsystem.emergencyalert.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AlertService alertService;

    // Get user profile using session token
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getProfile(@RequestHeader String sessionToken) {
        return ResponseEntity.ok(userService.getUserProfile(sessionToken));
    }

    @DeleteMapping("/alerts/{alertId}")
    public ResponseEntity<?> deleteAlert(
            @PathVariable Long alertId,
            @RequestHeader("sessionToken") String sessionToken
    ) {
        alertService.deleteAlert(alertId);  // implement this in your AlertService
        return ResponseEntity.ok(Map.of("message", "Alert deleted successfully"));
    }



    // Get predefined messages for a user
    @GetMapping("/predefined-messages")
    public ResponseEntity<List<String>> getPredefinedMessages(@RequestHeader String sessionToken) {
        return ResponseEntity.ok(userService.getPredefinedMessages(sessionToken));
    }

    // Update predefined messages (max 3)
    @PostMapping("/predefined-messages")
    public ResponseEntity<?> updatePredefinedMessages(
            @RequestHeader String sessionToken,
            @RequestBody List<String> messages
    ) {
        userService.updatePredefinedMessages(sessionToken, messages);
        return ResponseEntity.ok("Predefined messages updated successfully.");
    }


}
