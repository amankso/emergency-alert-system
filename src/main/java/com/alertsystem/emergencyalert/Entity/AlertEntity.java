package com.alertsystem.emergencyalert.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String mobileNumber;

    @Column(name = "custom_message", length = 500)
    private String customMessage;

    private Double latitude;
    private Double longitude;

    @Column(name = "map_url")
    private String mapUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity userEntity;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    // Store recipient numbers as a JSON string (instead of JSONB)
    @Column(name = "sent_to_json", columnDefinition = "TEXT")
    private String sentToJson;

    @Enumerated(EnumType.STRING)
    private AlertStatusEnum status = AlertStatusEnum.PENDING;

    @Column(name = "response_url")
    private String responseUrl; // frontend response URL

    @Column(name = "alert_timestamp")
    private LocalDateTime alertTimestamp;

    @Column(name = "nearby_police_stations", columnDefinition = "TEXT")
    private String nearbyPoliceStations;

    @Column(name = "region")
    private String region; // e.g., "Ghaziabad, Uttar Pradesh"

}
