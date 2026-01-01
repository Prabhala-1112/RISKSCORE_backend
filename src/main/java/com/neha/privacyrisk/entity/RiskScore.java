package com.neha.privacyrisk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_score")
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Target is required (App Name or URL)")
    @Column(name = "target", nullable = false)
    private String target; // App Name or Website URL

    @Column(name = "type")
    private String type; // APPLICATION or WEBSITE

    // RISK METRICS (0-100)

    @NotNull
    @Min(0)
    @Max(100)
    private Integer exposureLevel;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer userConsent;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer dataSensitivity;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer retentionPeriod;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer trackingRisk; // Third-party Tracking Risk

    @NotNull
    @Min(0)
    @Max(100)
    private Integer permissionRisk; // Permission Risk

    @NotNull
    @Min(0)
    @Max(100)
    private Integer networkSecurityRisk; // Network & Security Risk

    @Column(name = "final_risk_score")
    private Double finalRiskScore;

    @Column(name = "risk_category")
    private String riskCategory; // Low, Medium, High, Critical

    @Column(name = "description", length = 4096)
    private String description;

    @Column(name = "history", length = 4096)
    private String history;

    // Added for enhanced frontend display
    @Column(name = "category")
    private String category;

    @Column(name = "content_rating")
    private String contentRating;

    @Column(name = "tags")
    private String tags; // Comma separated related words

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getContentRating() {
        return contentRating;
    }

    public void setContentRating(String contentRating) {
        this.contentRating = contentRating;
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ✅ GETTERS & SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getExposureLevel() {
        return exposureLevel;
    }

    public void setExposureLevel(Integer exposureLevel) {
        this.exposureLevel = exposureLevel;
    }

    public Integer getUserConsent() {
        return userConsent;
    }

    public void setUserConsent(Integer userConsent) {
        this.userConsent = userConsent;
    }

    public Integer getDataSensitivity() {
        return dataSensitivity;
    }

    public void setDataSensitivity(Integer dataSensitivity) {
        this.dataSensitivity = dataSensitivity;
    }

    public Integer getRetentionPeriod() {
        return retentionPeriod;
    }

    public void setRetentionPeriod(Integer retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    public Integer getTrackingRisk() {
        return trackingRisk;
    }

    public void setTrackingRisk(Integer trackingRisk) {
        this.trackingRisk = trackingRisk;
    }

    public Integer getPermissionRisk() {
        return permissionRisk;
    }

    public void setPermissionRisk(Integer permissionRisk) {
        this.permissionRisk = permissionRisk;
    }

    public Integer getNetworkSecurityRisk() {
        return networkSecurityRisk;
    }

    public void setNetworkSecurityRisk(Integer networkSecurityRisk) {
        this.networkSecurityRisk = networkSecurityRisk;
    }

    public Double getFinalRiskScore() {
        return finalRiskScore;
    }

    public void setFinalRiskScore(Double finalRiskScore) {
        this.finalRiskScore = finalRiskScore;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
