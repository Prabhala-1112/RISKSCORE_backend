package com.neha.privacyrisk.service;

import com.neha.privacyrisk.entity.RiskScore;
import com.neha.privacyrisk.repository.RiskScoreRepository;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class RiskScoreService {

    private final RiskScoreRepository repository;

    private static final java.util.Map<String, RiskScore> KNOWN_APPS = new java.util.HashMap<>();

    static {
        // format: Target, Exposure, Consent, Sensitivity, Retention, Tracking,
        // Permission, Network, Type

        // --- SOCIAL MEDIA ---
        KNOWN_APPS.put("TIKTOK", createKnown("TikTok", "APPLICATION", 100, 75, 75, 100, 100, 90, 80));
        KNOWN_APPS.put("FACEBOOK", createKnown("Facebook", "APPLICATION", 100, 75, 75, 100, 100, 85, 80));
        KNOWN_APPS.put("INSTAGRAM", createKnown("Instagram", "APPLICATION", 75, 75, 75, 100, 90, 80, 75));
        KNOWN_APPS.put("X", createKnown("X (Twitter)", "APPLICATION", 100, 25, 25, 100, 80, 60, 50));

        // --- MESSAGING ---
        KNOWN_APPS.put("WHATSAPP", createKnown("WhatsApp", "APPLICATION", 75, 75, 25, 75, 50, 60, 20));
        KNOWN_APPS.put("SIGNAL", createKnown("Signal", "APPLICATION", 0, 0, 0, 0, 0, 10, 0));

        // --- SHOPPING ---
        KNOWN_APPS.put("AMAZON", createKnown("Amazon", "APPLICATION", 75, 50, 75, 100, 80, 90, 40));
        KNOWN_APPS.put("TEMU", createKnown("Temu", "APPLICATION", 100, 75, 75, 100, 100, 95, 90));

        // --- GOOGLE ---
        KNOWN_APPS.put("GOOGLE", createKnown("Google.com", "WEBSITE", 60, 50, 50, 100, 90, 20, 20));
    }

    public RiskScoreService(RiskScoreRepository repository) {
        this.repository = repository;
    }

    public RiskScore analyzeTarget(String target) {
        if (target == null || target.isBlank())
            return null;
        String key = target.trim().toUpperCase();

        // 1. Check Cache / Known List
        if (KNOWN_APPS.containsKey(key)) {
            return KNOWN_APPS.get(key);
        }

        // 2. Live Scan Simulation (Heuristic Engine)
        return performLiveScan(target);
    }

    private RiskScore performLiveScan(String target) {
        RiskScore score = new RiskScore();
        score.setTarget(target);

        boolean isUrl = target.startsWith("http") || target.contains(".");
        score.setType(isUrl ? "WEBSITE" : "APPLICATION");

        // Deterministic 'Random' based on target string hash for consistent
        // demonstration
        int hash = Math.abs(target.hashCode());
        Random random = new Random(hash);

        // Simulate scanning logic
        score.setExposureLevel(random.nextInt(40) + 20); // 20-60
        score.setUserConsent(random.nextInt(50) + 10);
        score.setDataSensitivity(isUrl ? random.nextInt(60) : random.nextInt(80)); // Apps usually higher
        score.setRetentionPeriod(random.nextInt(100));
        score.setTrackingRisk(random.nextInt(90) + 10);
        score.setPermissionRisk(isUrl ? 10 : random.nextInt(90));
        score.setNetworkSecurityRisk(random.nextInt(60));

        // Special keywords trigger higher risks
        String lower = target.toLowerCase();
        if (lower.contains("free") || lower.contains("crack") || lower.contains("game")) {
            score.setTrackingRisk(90);
            score.setExposureLevel(85);
        }
        if (lower.contains("bank") || lower.contains("pay")) {
            score.setDataSensitivity(100);
            score.setNetworkSecurityRisk(10); // Assume banks have good security usually, or maybe checked?
        }
        if (lower.startsWith("http://")) { // No SSL
            score.setNetworkSecurityRisk(95);
        }

        calculateFinalScore(score);

        try {
            repository.save(score);
        } catch (Exception e) {
            System.err.println("DB Save Failed (Non-fatal): " + e.getMessage());
        }

        return score;
    }

    private static RiskScore createKnown(String target, String type, int exposure, int consent, int sensitivity,
            int retention, int tracking, int permission, int network) {
        RiskScore score = new RiskScore();
        score.setTarget(target);
        score.setType(type);
        score.setExposureLevel(exposure);
        score.setUserConsent(consent);
        score.setDataSensitivity(sensitivity);
        score.setRetentionPeriod(retention);
        score.setTrackingRisk(tracking);
        score.setPermissionRisk(permission);
        score.setNetworkSecurityRisk(network);
        calculateFinalScore(score);
        return score;
    }

    private static void calculateFinalScore(RiskScore score) {
        // Weighted Average
        double total = score.getExposureLevel() * 1.5
                + score.getUserConsent() * 1.0
                + score.getDataSensitivity() * 1.2
                + score.getRetentionPeriod() * 0.8
                + score.getTrackingRisk() * 1.5
                + score.getPermissionRisk() * 1.2
                + score.getNetworkSecurityRisk() * 1.0;

        double divider = 1.5 + 1.0 + 1.2 + 0.8 + 1.5 + 1.2 + 1.0;
        double finalScore = total / divider;

        score.setFinalRiskScore(Math.min(100.0, finalScore)); // Cap at 100

        if (finalScore < 20)
            score.setRiskCategory("Low");
        else if (finalScore < 50)
            score.setRiskCategory("Medium");
        else if (finalScore < 80)
            score.setRiskCategory("High");
        else
            score.setRiskCategory("Critical");
    }

    // For Search Auto-complete
    public java.util.List<String> searchSuggestions(String query) {
        if (query == null || query.isBlank())
            return java.util.Collections.emptyList();
        String upper = query.toUpperCase();
        return KNOWN_APPS.keySet().stream()
                .filter(k -> k.contains(upper))
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
}
