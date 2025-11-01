package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    List<AlertEntity> findAllByUserEntity(UserEntity userEntity);
    List<AlertEntity> findAllByUserEntityAndStatus(UserEntity userEntity, AlertStatusEnum status);
    boolean existsByUserEntityAndStatus(UserEntity userEntity, AlertStatusEnum status);

}
