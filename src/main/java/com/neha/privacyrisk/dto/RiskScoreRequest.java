package com.neha.privacyrisk.dto;

import jakarta.validation.constraints.NotNull;

public class RiskScoreRequest {

    @NotNull(message = "Exposure Level is required")
    private String exposureLevel; // "LOW", "MEDIUM", "HIGH", "CRITICAL"

    @jakarta.validation.constraints.NotBlank(message = "Application Name is required")
    private String appName;

    @NotNull(message = "User Consent is required")
    private String userConsent; // "NO_CONSENT", "IMPLICIT", "EXPLICIT", "REVOCABLE"

    @NotNull(message = "Data Sensitivity is required")
    private String dataSensitivity; // "PUBLIC", "INTERNAL", "CONFIDENTIAL", "RESTRICTED"

    @NotNull(message = "Retention Period is required")
    private String retentionPeriod; // "NONE", "SHORT_TERM", "LONG_TERM", "INDEFINITE"

    // Getters and Setters
    public String getExposureLevel() {
        return exposureLevel;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setExposureLevel(String exposureLevel) {
        this.exposureLevel = exposureLevel;
    }

    public String getUserConsent() {
        return userConsent;
    }

    public void setUserConsent(String userConsent) {
        this.userConsent = userConsent;
    }

    public String getDataSensitivity() {
        return dataSensitivity;
    }

    public void setDataSensitivity(String dataSensitivity) {
        this.dataSensitivity = dataSensitivity;
    }

    public String getRetentionPeriod() {
        return retentionPeriod;
    }

    public void setRetentionPeriod(String retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }
}
