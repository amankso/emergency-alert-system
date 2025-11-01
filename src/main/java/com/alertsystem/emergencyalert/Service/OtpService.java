package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.Entity.OtpEntity;
import com.alertsystem.emergencyalert.Repository.OtpRepository;
import com.alertsystem.emergencyalert.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final int REQUEST_WINDOW_MINUTES = 15;

    @Transactional //telling Spring to run everything inside it as one atomic unit, anything fails rollback , commit on 100% success only
    //if same number enteredd and clcik for send otp at same time , or user clicked sentOtp double instantly , one will be blocked , no thread will be assigned to him
    public String generateOtp(String mobileNumber) {

        // Find existing OTP entry or create new
        OtpEntity entity = otpRepository.findByMobileNumber(mobileNumber)
                .orElseGet(() -> {
                    OtpEntity e = new OtpEntity();
                    e.setMobileNumber(mobileNumber);
                    e.setOtpRequestCount(0);
                    e.setVerified(false);
                    e.setFirstRequestAt(LocalDateTime.now());
                    return e;
                });

        // Check if 15-minute window expired, then reset count
        LocalDateTime now = LocalDateTime.now();
        if (entity.getFirstRequestAt() == null ||
                entity.getFirstRequestAt().isBefore(now.minusMinutes(REQUEST_WINDOW_MINUTES))) {
            entity.setFirstRequestAt(now);
            entity.setOtpRequestCount(0);
        }

        // Check abuse limit
        if (entity.getOtpRequestCount() >= MAX_REQUESTS_PER_WINDOW) {
            throw new BadRequestException("Too many OTP requests. Try again later.");
        }

        // Generate 6-digit numeric OTP
        String otp = generateNumericOtp();

        entity.setOtp(otp);
        entity.setExpiryTime(now.plusMinutes(OTP_EXPIRY_MINUTES));
        entity.setVerified(false);
        entity.setOtpRequestCount(entity.getOtpRequestCount() + 1);

        otpRepository.save(entity);

        log.debug("Generated OTP for {} expiring at {}", mobileNumber, entity.getExpiryTime());
        return otp; // note: in actual API, don’t return this to client; send via Twilio
    }

    @Transactional
    public boolean verifyOtp(String mobileNumber, String otp) {
        if (mobileNumber == null || otp == null) return false;

        Optional<OtpEntity> optionalEntity = otpRepository.findByMobileNumber(mobileNumber);
        if (optionalEntity.isEmpty()) return false;

        OtpEntity entity = optionalEntity.get();

        if (entity.isVerified()) return false;
        if (entity.getExpiryTime() == null || LocalDateTime.now().isAfter(entity.getExpiryTime())) return false;
        if (!entity.getOtp().equals(otp)) return false;

        entity.setVerified(true);
        otpRepository.save(entity);
        return true;
    }

    private String generateNumericOtp() {
        int min = (int) Math.pow(10, OTP_LENGTH - 1); // e.g., 100000
        int bound = 9 * min; // e.g., 900000
        int number = min + secureRandom.nextInt(bound);
        return String.valueOf(number);
    }

    @Scheduled(fixedRate = 60_000)  //Every 60 seconds
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = otpRepository.deleteByExpiryTimeBefore(now);
        if (deleted > 0) {
            log.info("Cleaned up {} expired OTP entries", deleted);
        }
    }
}
