package com.alertsystem.emergencyalert.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpDTO {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", message = "Invalid mobile number format")
    private String mobileNumber;

    // Optional field – only required during OTP verification
    private String otp;

    // Optional username – used when new user registers
    private String username;
}
