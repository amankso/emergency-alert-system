package com.alertsystem.emergencyalert.DTO;

import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PoliceAlertDTO {
    private Long id;
    private String alerteeName;
    private String mobileNumber;
    private String customMessage;
    private List<Map<String, String>> sentTo; // name, relation, mobile, status
    private String mapUrl;
    private LocalDateTime alertTimestamp;
    private String region;
    private AlertStatusEnum status;
}
