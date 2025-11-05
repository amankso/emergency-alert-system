package com.alertsystem.emergencyalert.Entity;

public enum UserRole {
    NORMAL_USER,
    POLICE_OFFICIAL;

    public boolean equalsIgnoreCase(String police) {
        return this.name().equalsIgnoreCase(police);
    }
}
