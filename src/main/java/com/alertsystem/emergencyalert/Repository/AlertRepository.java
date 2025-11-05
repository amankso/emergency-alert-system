package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.AlertEntity;
import com.alertsystem.emergencyalert.Entity.AlertStatusEnum;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> , JpaSpecificationExecutor<AlertEntity> {

    // ---------- BASIC ----------
    Optional<AlertEntity> findById(Long id);
    List<AlertEntity> findAllByUserEntity(UserEntity user);
    List<AlertEntity> findAllByUserEntityAndStatusIn(UserEntity user, List<AlertStatusEnum> statuses);
    List<AlertEntity> findAllByUserEntityOrderByAlertTimestampDesc(UserEntity user);

    boolean existsByUserEntityAndStatus(UserEntity user, AlertStatusEnum status);

    // ---------- PAGINATED FILTERS ----------
    Page<AlertEntity> findByUserEntity_MobileNumberContainingIgnoreCase(String mobileNumber, Pageable pageable);
    Page<AlertEntity> findByStatus(AlertStatusEnum status, Pageable pageable);
    Page<AlertEntity> findByAlertTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<AlertEntity> findAll(Pageable pageable);

    // Region-based filtering
    Page<AlertEntity> findByRegionContainingIgnoreCase(String region, Pageable pageable);
    Page<AlertEntity> findByRegionIgnoreCaseAndStatus(String region, AlertStatusEnum status, Pageable pageable);

    // ---------- POLICE-SPECIFIC ----------
    @Query("SELECT a FROM AlertEntity a WHERE a.status = :status AND a.nearbyPoliceStations IS NOT NULL")
    List<AlertEntity> findAllByStatusWithNearbyStations(@Param("status") AlertStatusEnum status);

    @Query("SELECT a FROM AlertEntity a WHERE LOWER(a.nearbyPoliceStations) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AlertEntity> searchByNearbyStationName(@Param("keyword") String keyword);

    // ---------- DATE ----------
    List<AlertEntity> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    Page<AlertEntity> findAll(Specification<AlertEntity> spec, Pageable pageable);
    // Count by status
    long countByStatus(AlertStatusEnum status);

    // Count alerts between timestamps
    long countByAlertTimestampBetween(LocalDateTime start, LocalDateTime end);
}
