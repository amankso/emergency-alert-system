package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertResponseRepository extends JpaRepository<AlertResponseEntity, Long> {
    List<AlertResponseEntity> findByAlert(AlertEntity alert);
}
