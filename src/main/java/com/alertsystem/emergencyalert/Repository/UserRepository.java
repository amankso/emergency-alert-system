package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByMobileNumber(String mobileNumber);

    Optional<UserEntity> findByMobileNumberAndIsVerifiedTrue(String mobileNumber);

    boolean existsByMobileNumber(String mobileNumber);

//    Optional<UserEntity> findByActivationToken(String token);
//    // → for account verification via link or OTP.

    void deleteByMobileNumber(String mobileNumber);
    // → optional cleanup route (e.g. if user re-registers)

    Optional<UserEntity> findBySessionToken(String sessionToken);

}
