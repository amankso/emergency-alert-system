package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.DTO.AlertDTO;
import com.alertsystem.emergencyalert.Entity.*;
import com.alertsystem.emergencyalert.Repository.AlertRepository;
import com.alertsystem.emergencyalert.Repository.ContactRepository;
import com.alertsystem.emergencyalert.Util.AlertUrlUtil;
import com.alertsystem.emergencyalert.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final ContactRepository contactRepository;
    private final AlertRepository alertRepository;
    private final TwilioService twilioService;
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Transactional
    public AlertEntity sendAlert(String sessionToken, AlertDTO dto) throws JsonProcessingException {

        // Identify current logged-in user
        UserEntity user = authService.getUserBySessionToken(sessionToken);

        // Fetch user's emergency contacts
        List<ContactEntity> contacts = contactRepository.findAllByUserEntity(user);
        if (contacts == null || contacts.isEmpty()) {
            throw new ResourceNotFoundException("No emergency contacts found for user");
        }

        // Create alert record
        AlertEntity alert = AlertEntity.builder()
                .userEntity(user)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .customMessage(dto.getCustomMessage())
                .mapUrl(buildGoogleMapsUrl(dto.getLatitude(), dto.getLongitude()))
                .status(AlertStatusEnum.PENDING)
                .alertTimestamp(LocalDateTime.now())
                .build();

        alertRepository.save(alert);

        // Track send results
        List<String> sentNumbers = new ArrayList<>();
        List<String> failedNumbers = new ArrayList<>();

        // Send SMS to all contacts
        for (ContactEntity contact : contacts) {
            String responseUrl = AlertUrlUtil.buildResponseUrlForContact(
                    frontendBaseUrl, alert.getId(), contact.getMobileNumber()
            );

            String message = buildAlertSms(
                    user.getUsername(),
                    dto.getCustomMessage(),
                    alert.getMapUrl(),
                    responseUrl,
                    LocalDateTime.now()
            );

            try {
                twilioService.sendSms(contact.getMobileNumber(), message);
                log.info("✅ Alert SMS sent to {}", contact.getMobileNumber());
                sentNumbers.add(contact.getMobileNumber());
            } catch (Exception ex) {
                log.error("❌ Failed to send SMS to {}: {}", contact.getMobileNumber(), ex.getMessage());
                failedNumbers.add(contact.getMobileNumber());
            }
        }

        // Determine final status
        if (!sentNumbers.isEmpty()) {
            alert.setStatus(AlertStatusEnum.SENT);
        } else {
            alert.setStatus(AlertStatusEnum.FAILED);
        }

        // Save which contacts were sent/failed
        alert.setSentToJson(objectMapper.writeValueAsString(sentNumbers));

        return alertRepository.save(alert);
    }

    public List<AlertEntity> getUserAlerts(String sessionToken) {
        UserEntity user = authService.getUserBySessionToken(sessionToken);
        return alertRepository.findAllByUserEntity(user);
    }

    public List<AlertEntity> getActiveAlerts(String sessionToken) {
        UserEntity user = authService.getUserBySessionToken(sessionToken);
        return alertRepository.findAllByUserEntityAndStatus(user, AlertStatusEnum.SENT);
    }

    private String buildGoogleMapsUrl(double lat, double lng) {
        return String.format("https://www.google.com/maps?q=%f,%f", lat, lng);
    }

    private String buildAlertSms(String username, String message, String mapUrl, String responseUrl, LocalDateTime time) {
        String timestamp = time.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"));
        return String.format(
                "🚨 Alert from %s:\n%s\n📍 %s\n⏰ %s\nRespond: %s",
                username,
                (message == null || message.isBlank()) ? "No custom message provided." : message,
                mapUrl,
                timestamp,
                responseUrl
        );
    }
}
