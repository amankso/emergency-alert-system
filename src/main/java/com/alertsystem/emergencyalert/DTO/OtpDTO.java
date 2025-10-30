package com.alertsystem.emergencyalert.DTO;

import jakarta.validation.constraints.NotBlank;
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
    private String mobileNumber;

    private String otp; // optional — used only during verification
}
