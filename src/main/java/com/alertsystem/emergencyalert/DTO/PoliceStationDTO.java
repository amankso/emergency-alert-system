package com.alertsystem.emergencyalert.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoliceStationDTO {
    private Long alertId;
    private List<String> nearbyStations;
}
