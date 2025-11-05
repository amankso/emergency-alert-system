package com.alertsystem.emergencyalert.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "police_stations")
@Builder
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class PoliceStationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    // Use BigDecimal to preserve full precision
    @Column(precision = 17, scale = 14, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 17, scale = 14, nullable = false)
    private BigDecimal longitude;

    @Column(name = "contact_number",  length = 20)
    private String contactNumber;

}
