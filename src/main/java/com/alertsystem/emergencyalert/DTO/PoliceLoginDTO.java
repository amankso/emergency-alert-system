package com.alertsystem.emergencyalert.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PoliceLoginDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @NotBlank(message = "Session token is required")
    private String sessionToken;

    private String otp; // null before sending OTP
}
