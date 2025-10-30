package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    List<AlertEntity> findAllByStatus(AlertStatusEnum status);

    List<AlertEntity> findAllByUserEntity(UserEntity userEntity);
    // → get user’s past alerts.

    List<AlertEntity> findAllByUserEntityAndStatus(UserEntity userEntity, AlertStatusEnum status);
    // → for filtering active/resolved alerts.

    boolean existsByUserEntityAndStatus(UserEntity userEntity, AlertStatusEnum status);
    // → to check if a user already has an active alert (prevent spamming).

    void deleteByUserEntity(UserEntity userEntity);
    // → cleanup when deleting user.

}
