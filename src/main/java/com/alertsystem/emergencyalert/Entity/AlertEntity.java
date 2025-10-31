package com.alertsystem.emergencyalert.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @Column(columnDefinition = "jsonb")
    private List<String> sentTo;

    @Enumerated(EnumType.STRING)
    private AlertStatusEnum status = AlertStatusEnum.PENDING;

    @Column(name = "response_url")
    private String responseUrl; // frontend response URL

    @Column(name = "alert_timestamp")
    private LocalDateTime alertTimestamp;

}
