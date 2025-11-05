package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.DTO.PoliceLoginDTO;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import com.alertsystem.emergencyalert.Entity.UserRole;
import com.alertsystem.emergencyalert.Repository.UserRepository;
import com.alertsystem.emergencyalert.exception.BadRequestException;
import com.alertsystem.emergencyalert.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final TwilioService twilioService;

    /** 🔹 Send OTP for signup (new user registration) */
    @Transactional
    public void sendOtpForSignup(String username, String mobileNumber) {
        validateMobile(mobileNumber);

        // If user exists & verified => already registered
        userRepository.findByMobileNumber(mobileNumber).ifPresent(user -> {
            if (user.isVerified()) throw new BadRequestException("User already exists");
        });

        // Generate & save OTP (via OtpService)
        String otp = otpService.generateOtp(mobileNumber);
        twilioService.sendSms(mobileNumber, "Your OTP for EmergencyAlert signup is " + otp + ". Expires in 5 minutes.");

        log.info("Signup OTP sent to {}", mobileNumber);
    }

    /** 🔹 Verify OTP (for both signup & login) and create session */
    @Transactional
    public AuthResult verifyOtpAndCreateSession(String mobile, String otp, String usernameIfNew, String role) {
        if (!otpService.verifyOtp(mobile, otp))
            throw new BadRequestException("Invalid or expired OTP");

        // Fetch or create user
        UserEntity user = userRepository.findByMobileNumber(mobile)
                .orElseGet(() -> userRepository.save(UserEntity.builder()
                        .mobileNumber(mobile)
                        .username(usernameIfNew == null ? mobile : usernameIfNew)
                        .createdDate(LocalDateTime.now())
                        .isVerified(true)
                        .build()
                ));

        // Mark verified & assign session token
        user.setVerified(true);
        user.setSessionToken(UUID.randomUUID().toString());

        // ⚡ Set role correctly
        if (role != null && role.equalsIgnoreCase("POLICE_OFFICIAL")) {
            user.setRole(UserRole.POLICE_OFFICIAL);
        } else if(user.getRole() == null) {
            // Default for new user
            user.setRole(UserRole.NORMAL_USER);
        }

        userRepository.save(user);

        log.info("User {} logged in successfully as {}", user.getUsername(), user.getRole());
        return new AuthResult(user.getId(), user.getUsername(), user.getMobileNumber(), user.getSessionToken(), user.getRole());
    }


    /** 🔹 Fetch user by session token (for authenticated requests) */
    public UserEntity getUserBySessionToken(String token) {
        if (token == null || token.isBlank()) throw new BadRequestException("Session token required");
        return userRepository.findBySessionToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired session token"));
    }

    /** 🔹 Logout (invalidate session) */
    @Transactional
    public void logout(String sessionToken) {
        UserEntity user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        user.setSessionToken(null);
        userRepository.save(user);
        log.info("User {} logged out", user.getUsername());
    }

    /** 🔹 Send OTP for login only */
    @Transactional
    public Map<String, Object> sendOtpForLogin(String mobileNumber) {
        validateMobile(mobileNumber);

        UserEntity user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found. Please register first."));

        String otp = otpService.generateOtp(mobileNumber);
        twilioService.sendSms(mobileNumber, "Your OTP for EmergencyAlert login is " + otp + ". Expires in 5 minutes.");

        log.info("Login OTP sent to {}", mobileNumber);

        return Map.of(
                "message", "OTP sent successfully",
                "mobileNumber", mobileNumber,
                "timestamp", LocalDateTime.now()
        );
    }

    /** Helper compact model */
    public record AuthResult(Long userId, String username, String mobile, String sessionToken, UserRole role) {}

    /** Helper validation */
    private void validateMobile(String mobile) {
        if (mobile == null || mobile.isBlank())
            throw new BadRequestException("Mobile number is required");
        if (!mobile.matches("^[6-9]\\d{9}$"))
            throw new BadRequestException("Invalid mobile number format");
    }

    @Transactional
    public Map<String, Object> verifyPoliceCredentials(PoliceLoginDTO dto) {
        UserEntity user = userRepository.findByMobileNumber(dto.getMobileNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Police not found"));

        if (!user.getUsername().equalsIgnoreCase(dto.getUsername()))
            throw new BadRequestException("Username mismatch");

        if (user.getRole() != UserRole.POLICE_OFFICIAL)
            throw new BadRequestException("Not authorized as police");

        if (user.getSessionToken() == null || !user.getSessionToken().equals(dto.getSessionToken()))
            throw new BadRequestException("Invalid session token");

        // ✅ if all checks pass, send OTP
        String otp = otpService.generateOtp(dto.getMobileNumber());
        twilioService.sendSms(dto.getMobileNumber(),
                "Your OTP for EmergencyAlert (Police Login) is " + otp + ". Expires in 5 minutes.");

        log.info("OTP sent successfully to police {}", dto.getMobileNumber());

        return Map.of(
                "message", "OTP sent successfully",
                "mobileNumber", dto.getMobileNumber(),
                "timestamp", LocalDateTime.now()
        );
    }


}
