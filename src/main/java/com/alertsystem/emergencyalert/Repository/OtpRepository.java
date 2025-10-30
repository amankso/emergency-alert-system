package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findByMobileNumber(String mobileNumber);

    void deleteByExpiresAtBefore(LocalDateTime now);

    // → after successful verification, cleanup old OTP.
    void deleteByMobileNumber(String mobileNumber);

}
