package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.Entity.UserEntity;
import com.alertsystem.emergencyalert.exception.BadRequestException;
import com.alertsystem.emergencyalert.exception.ResourceNotFoundException;
import com.alertsystem.emergencyalert.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final TwilioService twilioService;

    // send signup or login OTP
    @Transactional
    public void sendOtpForSignup(String username, String mobileNumber) {
        if (mobileNumber == null || mobileNumber.isBlank()) throw new BadRequestException("mobile required");
        userRepository.findByMobileNumber(mobileNumber).ifPresent(u -> {
            if (Boolean.TRUE.equals(u.isVerified())) throw new BadRequestException("User already exists");
        });

        String otp = otpService.generateOtp(mobileNumber);
        String message = String.format("Your OTP for EmergencyAlert is %s. Expires in 5 minutes.", otp);
        twilioService.sendSms(mobileNumber, message);
    }

    @Transactional
    public AuthResult verifyOtpAndCreateSession(String mobileNumber, String otp, String usernameIfNew) {
        boolean ok = otpService.verifyOtp(mobileNumber, otp);
        if (!ok) throw new BadRequestException("Invalid or expired OTP");

        UserEntity user = userRepository.findByMobileNumber(mobileNumber).orElseGet(() -> {
            UserEntity u = new UserEntity();
            u.setMobileNumber(mobileNumber);
            u.setUsername(usernameIfNew == null ? mobileNumber : usernameIfNew);
            u.setVerified(true);
            return userRepository.save(u);
        });

        // mark verified if not already
        if (!Boolean.TRUE.equals(user.isVerified())) {
            user.setVerified(true);
        }

        // generate session token
        String token = UUID.randomUUID().toString();
        user.setSessionToken(token);
        user.setCreatedDate(user.getCreatedDate() == null ? LocalDateTime.now() : user.getCreatedDate());
        userRepository.save(user);

        log.info("User {} logged in (mobile={})", user.getUsername(), mobileNumber);
        return new AuthResult(user.getId(), user.getUsername(), user.getMobileNumber(), token);
    }

    public UserEntity getUserBySessionToken(String token) {
        if (token == null || token.isBlank()) throw new BadRequestException("Token required");
        return userRepository.findBySessionToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid session token"));
    }

    @Transactional
    public void logout(String sessionToken) {
        UserEntity user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        user.setSessionToken(null);
        userRepository.save(user);
    }

    // small DTO
    public static record AuthResult(Long userId, String username, String mobile, String sessionToken) {}
}
