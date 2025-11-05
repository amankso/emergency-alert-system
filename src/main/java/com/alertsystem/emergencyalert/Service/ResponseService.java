package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.Entity.*;
import com.alertsystem.emergencyalert.Repository.*;
import com.alertsystem.emergencyalert.exception.BadRequestException;
import com.alertsystem.emergencyalert.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseService {

    private final AlertRepository alertRepository;
    private final AlertResponseRepository responseRepository;
    private final ContactRepository contactRepository;

    /**
     * Save a custom response from a contacted person.
     */
    @Transactional
    public AlertResponseEntity submitResponse(Long alertId, String responderPhone, String responseMessage) {

        if (responseMessage == null || responseMessage.isBlank()) {
            throw new BadRequestException("Response message cannot be empty");
        }

        AlertEntity alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        // Verify that responderPhone belongs to one of alert’s saved contacts
        boolean isValidResponder = contactRepository.existsByUserEntityAndMobileNumber(
                alert.getUserEntity(), responderPhone
        );
        if (!isValidResponder) {
            throw new BadRequestException("Responder not authorized for this alert");
        }

        // Get contact name if available
        ContactEntity contact = contactRepository.findByMobileNumber(responderPhone).orElse(null);

        AlertResponseEntity response = AlertResponseEntity.builder()
                .alert(alert)
                .alerter(alert.getUserEntity())
                .responderPhone(responderPhone)
                .responderName(contact != null ? contact.getName() : "Unknown Contact")
                .responseMessage(responseMessage)
                .responseTime(LocalDateTime.now())
                .build();

        AlertResponseEntity saved = responseRepository.save(response);

        // Update alert status if first response received
        if (alert.getStatus() == AlertStatusEnum.PENDING || alert.getStatus() == AlertStatusEnum.SENT) {
            alert.setStatus(AlertStatusEnum.RESOLVED);
            alertRepository.save(alert);
        }

        log.info("Response recorded for alert {} by {}", alertId, responderPhone);
        return saved;
    }

    /**
     * Fetch all responses for a given alert.
     */
    public List<AlertResponseEntity> getResponsesByAlert(Long alertId) {
        AlertEntity alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        return responseRepository.findByAlert(alert);
    }
}
