package com.alertsystem.emergencyalert.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(unique = true)
    private String mobileNumber;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "is_verified")
    private boolean isVerified;

    @Column(name = "session_token", unique = true)
    private String sessionToken;          // random token generated after login

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactEntity> contacts;


}
