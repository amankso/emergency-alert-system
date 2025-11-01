package com.alertsystem.emergencyalert.Util;

import java.util.Base64;

public class AlertUrlUtil {

    public static String buildResponseUrlForContact(String frontendBaseUrl, Long alertId, String contactPhone) {
        String token = generateResponseToken(alertId, contactPhone);
        return String.format("%s/respond/%d?token=%s", frontendBaseUrl, alertId, token);
    }

    private static String generateResponseToken(Long alertId, String contactPhone) {
        String raw = alertId + ":" + contactPhone + ":" + System.currentTimeMillis();
        return Base64.getUrlEncoder().encodeToString(raw.getBytes());
    }
}
