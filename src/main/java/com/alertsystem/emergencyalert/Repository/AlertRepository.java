package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    // Existing methods
    List<AlertEntity> findAllByUserEntity(UserEntity userEntity);
    List<AlertEntity> findAllByUserEntityAndStatus(UserEntity userEntity, AlertStatusEnum status);
    boolean existsByUserEntityAndStatus(UserEntity userEntity, AlertStatusEnum status);

    //Find alert by ID
    Optional<AlertEntity> findById(Long id);

    //For Police Dashboard: find all alerts by status (e.g. PENDING, RESOLVED)
    List<AlertEntity> findAllByStatus(AlertStatusEnum status);

    //Filter by status and non-null nearby police stations
    @Query("SELECT a FROM AlertEntity a WHERE a.status = :status AND a.nearbyPoliceStations IS NOT NULL")
    List<AlertEntity> findAllByStatusWithNearbyStations(@Param("status") AlertStatusEnum status);

    //For searching alerts by keyword in nearby police stations list
    @Query("SELECT a FROM AlertEntity a WHERE LOWER(a.nearbyPoliceStations) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AlertEntity> searchByNearbyStationName(@Param("keyword") String keyword);
}
