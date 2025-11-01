package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.OtpEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    // find OTP record by mobile number
    Optional<OtpEntity> findByMobileNumber(String mobileNumber);

    // delete all expired OTPs (use expiryTime field)
    @Transactional
    @Modifying
    int deleteByExpiryTimeBefore(LocalDateTime now);

    // delete OTP after successful verification
    @Transactional
    void deleteByMobileNumber(String mobileNumber);

}
