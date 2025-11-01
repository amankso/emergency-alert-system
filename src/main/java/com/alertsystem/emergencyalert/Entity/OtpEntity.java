package com.alertsystem.emergencyalert.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String mobileNumber;
    private String otp;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    private boolean isVerified;

    private int otpRequestCount;

    private LocalDateTime firstRequestAt;
}
