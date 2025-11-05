package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.DTO.AlertDTO;
import com.alertsystem.emergencyalert.DTO.PoliceAlertDTO;
import com.alertsystem.emergencyalert.Entity.*;
import com.alertsystem.emergencyalert.Repository.AlertRepository;
import com.alertsystem.emergencyalert.Repository.ContactRepository;
import com.alertsystem.emergencyalert.Repository.UserRepository;
import com.alertsystem.emergencyalert.Util.AlertUrlUtil;
import com.alertsystem.emergencyalert.Util.LocationResolver;
import com.alertsystem.emergencyalert.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final ContactRepository contactRepository;
    private final AlertRepository alertRepository;
    private final TwilioService twilioService;
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LocationResolver locationResolver;
    private final ReverseGeocodingService reverseGeocodingService;
    private final UserRepository userRepository;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    /** 🔹 Get all alerts of a user */
    public List<AlertEntity> getUserAlerts(String sessionToken) {
        UserEntity user = authService.getUserBySessionToken(sessionToken);
        return alertRepository.findAllByUserEntityOrderByAlertTimestampDesc(user);
    }

    /** 🔹 Get only active alerts (status = PENDING or FAILED) */
    public List<AlertEntity> getActiveAlerts(String sessionToken) {
        UserEntity user = authService.getUserBySessionToken(sessionToken);
        return alertRepository.findAllByUserEntityAndStatusIn(
                user,
                List.of(AlertStatusEnum.PENDING, AlertStatusEnum.FAILED)
        );
    }


    public void deleteAlert(Long alertId) {
        alertRepository.deleteById(alertId);
    }

    /** 🔹 Send alert */
    @Transactional
    public AlertEntity sendAlert(String sessionToken, AlertDTO dto) throws JsonProcessingException {
        UserEntity user = authService.getUserBySessionToken(sessionToken);

        List<ContactEntity> contacts = contactRepository.findAllByUserEntity(user);
        if (contacts == null || contacts.isEmpty()) {
            throw new ResourceNotFoundException("No emergency contacts found for this user");
        }

        AlertEntity alert = AlertEntity.builder()
                .userEntity(user)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .customMessage(dto.getCustomMessage())
                .mapUrl(buildGoogleMapsUrl(dto.getLatitude(), dto.getLongitude()))
                .status(AlertStatusEnum.PENDING)
                .alertTimestamp(LocalDateTime.now())
                .region(reverseGeocodingService.getRegionFromCoordinates(dto.getLatitude(), dto.getLongitude()))
                .build();

        alertRepository.save(alert);

        List<Map<String, String>> sentToList = new ArrayList<>();
        for (ContactEntity contact : contacts) {
            String responseUrl = AlertUrlUtil.buildResponseUrlForContact(
                    frontendBaseUrl, alert.getId(), contact.getMobileNumber()
            );
            String smsBody = buildAlertSms(user.getUsername(), dto.getCustomMessage(), alert.getMapUrl(), responseUrl, LocalDateTime.now());

            try {
                twilioService.sendSms(contact.getMobileNumber(), smsBody);
                log.info("✅ SMS sent to {}", contact.getMobileNumber());
                sentToList.add(Map.of(
                        "name", contact.getName(),
                        "relation", contact.getRelation(),
                        "mobileNumber", contact.getMobileNumber(),
                        "status", "SENT"
                ));
            } catch (Exception ex) {
                log.error("❌ Failed to send SMS to {}: {}", contact.getMobileNumber(), ex.getMessage());
                sentToList.add(Map.of(
                        "name", contact.getName(),
                        "relation", contact.getRelation(),
                        "mobileNumber", contact.getMobileNumber(),
                        "status", "FAILED"
                ));
            }
        }

        alert.setSentToJson(objectMapper.writeValueAsString(sentToList));
        alert.setStatus(sentToList.stream().allMatch(c -> c.get("status").equals("FAILED"))
                ? AlertStatusEnum.FAILED
                : AlertStatusEnum.PENDING);

        return alertRepository.save(alert);
    }

    private String buildGoogleMapsUrl(double lat, double lng) {
        return String.format("https://www.google.com/maps?q=%f,%f", lat, lng);
    }

    private String buildAlertSms(String username, String message, String mapUrl, String responseUrl, LocalDateTime time) {
        String timestamp = time.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"));
        return String.format("🚨 Alert from %s:\n%s\n📍 %s\n⏰ %s\nRespond: %s",
                username,
                (message == null || message.isBlank()) ? "No custom message provided." : message,
                mapUrl,
                timestamp,
                responseUrl
        );
    }

    /** 🔹 Paginated & filtered alerts for police dashboard */
    public Page<PoliceAlertDTO> getAlertsForPolice(
            String mobileNumber,
            String status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            String region
    ) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("alertTimestamp").descending());

        Specification<AlertEntity> spec = null; // start with null

        if (mobileNumber != null && !mobileNumber.isBlank()) {
            Specification<AlertEntity> mobileSpec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("userEntity").get("mobileNumber")),
                            "%" + mobileNumber.toLowerCase() + "%");
            spec = (spec == null) ? mobileSpec : spec.and(mobileSpec);
        }

        if (status != null && !status.isBlank()) {
            // user selected a status, use it
            Specification<AlertEntity> statusSpec = (root, query, cb) ->
                    cb.equal(root.get("status"), AlertStatusEnum.valueOf(status.toUpperCase()));
            spec = (spec == null) ? statusSpec : spec.and(statusSpec);
        } else {
            // no status selected => exclude RESOLVED
            Specification<AlertEntity> excludeResolved = (root, query, cb) ->
                    cb.notEqual(root.get("status"), AlertStatusEnum.RESOLVED);
            spec = (spec == null) ? excludeResolved : spec.and(excludeResolved);
        }


        if (from != null && to != null) {
            Specification<AlertEntity> dateSpec = (root, query, cb) ->
                    cb.between(root.get("alertTimestamp"), from, to);
            spec = (spec == null) ? dateSpec : spec.and(dateSpec);
        }

        if (region != null && !region.isBlank()) {
            Specification<AlertEntity> regionSpec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("region")), "%" + region.toLowerCase() + "%");
            spec = (spec == null) ? regionSpec : spec.and(regionSpec);
        }

// Execute query
        Page<AlertEntity> alerts;
        if (spec != null) {
            alerts = alertRepository.findAll(spec, pageable);
        } else {
            alerts = alertRepository.findAll(pageable);
        }



        return alerts.map(alert -> {
            List<Map<String, String>> sentToList = new ArrayList<>();
            try {
                if (alert.getSentToJson() != null)
                    sentToList = objectMapper.readValue(alert.getSentToJson(), List.class);
            } catch (Exception e) {
                log.error("Error parsing sentToJson for alert {}: {}", alert.getId(), e.getMessage());
            }

            return PoliceAlertDTO.builder()
                    .id(alert.getId()) // ✅ add this
                    .alerteeName(alert.getUserEntity().getUsername())
                    .mobileNumber(alert.getUserEntity().getMobileNumber())
                    .customMessage(alert.getCustomMessage())
                    .sentTo(sentToList)
                    .mapUrl(alert.getMapUrl())
                    .alertTimestamp(alert.getAlertTimestamp())
                    .region(alert.getRegion())
                    .status(AlertStatusEnum.valueOf(alert.getStatus().name()))
                    .build();
        });

    }

    @Transactional
    public void markAsResolved(Long alertId) {
        AlertEntity alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found"));
        alert.setStatus(AlertStatusEnum.RESOLVED);
        alertRepository.save(alert);
    }

    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalAlerts", alertRepository.count());
        stats.put("pendingAlerts", alertRepository.countByStatus(AlertStatusEnum.PENDING));
        stats.put("resolvedAlerts", alertRepository.countByStatus(AlertStatusEnum.RESOLVED));
        // TODO: if you track spam reports, filter users by isReportedSpam
        stats.put("spamReports", userRepository.count());
        stats.put("todayAlerts", alertRepository.countByAlertTimestampBetween(
                LocalDate.now().atStartOfDay(),
                LocalDateTime.now()
        ));
        return stats;
    }

    public List<AlertEntity> getFilteredAlerts(String status, String region) {
        if ((status == null || status.isBlank()) && (region == null || region.isBlank())) {
            return alertRepository.findAll();
        } else if (status != null && !status.isBlank() && (region == null || region.isBlank())) {
            return alertRepository.findByStatus(AlertStatusEnum.valueOf(status.toUpperCase()), Pageable.unpaged()).getContent();
        } else if ((status == null || status.isBlank()) && region != null && !region.isBlank()) {
            return alertRepository.findByRegionContainingIgnoreCase(region, Pageable.unpaged()).getContent();
        } else {
            // Filter both status + region
            return alertRepository.findAll((root, query, cb) ->
                    cb.and(
                            cb.equal(root.get("status"), AlertStatusEnum.valueOf(status.toUpperCase())),
                            cb.like(cb.lower(root.get("region")), "%" + region.toLowerCase() + "%")
                    ), Pageable.unpaged()).getContent();
        }
    }




}
