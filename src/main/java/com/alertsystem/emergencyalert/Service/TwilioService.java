package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.exception.SmsSendException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    private volatile boolean configured = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && authToken != null) {
            Twilio.init(accountSid, authToken);
            configured = true;
            log.info("Twilio initialized");
        } else {
            log.warn("Twilio not configured (missing credentials). Running in mock mode.");
        }
    }

    public void sendSms(String to, String body) {
        // ensure proper +91 prefix for Indian numbers
        if (!to.startsWith("+91")) {
            to = "+91" + to;
        }

        if (!configured) {
            log.info("[MOCK SMS] to={} body={}", to, body);
            return;
        }

        try {
            Message.creator(
                    new com.twilio.type.PhoneNumber(to),
                    new com.twilio.type.PhoneNumber(fromPhoneNumber),
                    body
            ).create();
            log.info("Sent SMS to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send SMS to {} : {}", to, ex.getMessage());
            throw new SmsSendException("Failed to send SMS", ex);
        }
    }

    public void sendBulkSms(List<String> recipients, String body) {
        for (String r : recipients) {
            sendSms(r, body);
        }
    }
}
