package com.alertsystem.emergencyalert.exception;

public class SmsSendException extends RuntimeException {
    public SmsSendException(String msg, Throwable cause) { super(msg, cause); }
    public SmsSendException(String msg) { super(msg); }
}
