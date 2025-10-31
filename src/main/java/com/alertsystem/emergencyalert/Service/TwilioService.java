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

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String fromPhoneNumber;

    private volatile boolean configured = false;

    //at application startup , when TwilioService bean will be stored,this method will run instantly after storage .It will provide the twilio congurations (like from which twilio accunt i will be sending the sms)
    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank() && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            configured = true;
            log.info("Twilio initialized");
        } else {
            log.warn("Twilio not configured (missing credentials). TwilioService will not send real SMS.");
        }
    }

    //Basic sms sending , if not configured send to log else to actual number
    public void sendSms(String to, String body) {
        if (!configured) {
            log.info("[MOCK SMS] to={} body={}", to, body);
            return; // mock mode — log only
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

    //to send to multiple contacts , send one by one , you cant message all at once as receipents might be from diff address so limited by twilio , thus you can use async (completablefuture) for less delay if contacts are more
    public void sendBulkSms(List<String> recipients, String body) {
        for (String r : recipients) {
            sendSms(r, body);
        }
    }
}
