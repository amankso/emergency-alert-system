package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.OtpDTO;
import com.alertsystem.emergencyalert.DTO.PoliceLoginDTO;
import com.alertsystem.emergencyalert.DTO.UserDTO;
import com.alertsystem.emergencyalert.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register user + trigger OTP
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        authService.sendOtpForSignup(userDTO.getUsername(), userDTO.getMobileNumber());
        return ResponseEntity.ok("OTP sent successfully");
    }

    // Verify OTP + create session
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpDTO otpDTO) {
        return ResponseEntity.ok(
                authService.verifyOtpAndCreateSession(
                        otpDTO.getMobileNumber(),
                        otpDTO.getOtp(),
                        otpDTO.getUsername(),
                        (otpDTO.getRole() == null || otpDTO.getRole().trim().isEmpty())
                                ? "USER"
                                : otpDTO.getRole().trim().toUpperCase()
                )
        );
    }

    // Login user (mobile + OTP)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody OtpDTO otpDTO) {
        return ResponseEntity.ok(
                authService.verifyOtpAndCreateSession(
                        otpDTO.getMobileNumber(),
                        otpDTO.getOtp(),
                        null,
                        (otpDTO.getRole() == null || otpDTO.getRole().trim().isEmpty())
                                ? "USER"
                                : otpDTO.getRole().trim().toUpperCase()
                )
        );

    }

    // Logout user
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader String sessionToken) {
        authService.logout(sessionToken);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String mobileNumber = request.get("mobileNumber");
        return ResponseEntity.ok(authService.sendOtpForLogin(mobileNumber));
    }

    @PostMapping("/verify-police")
    public ResponseEntity<?> verifyPoliceCredentials(@RequestBody PoliceLoginDTO dto) {
        return ResponseEntity.ok(authService.verifyPoliceCredentials(dto));
    }



}
