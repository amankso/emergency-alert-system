package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.UserDTO;
import com.alertsystem.emergencyalert.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get user profile using session token
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(@RequestHeader String sessionToken) {
        return ResponseEntity.ok(userService.getUserProfile(sessionToken));
    }

    // Delete user (if needed)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestHeader String sessionToken) {
        userService.deleteUser(sessionToken);
        return ResponseEntity.ok("User deleted successfully.");
    }
}
