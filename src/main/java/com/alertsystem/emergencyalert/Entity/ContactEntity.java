package com.alertsystem.emergencyalert.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contact_entity",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_mobile", columnNames = {"user_id","mobile_number"}))
public class ContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // cannot do @Column(unique = true) Impact: Adds wrong DB constraint, will throw exceptions when another user saves same contact number.
    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;
    private String relation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private UserEntity userEntity;


}
