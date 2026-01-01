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
        // Permission, Network, Type, Description, History

        // --- SOCIAL MEDIA ---
        KNOWN_APPS.put("TIKTOK", createKnown("TikTok", "APPLICATION", 100, 75, 75, 100, 100, 90, 80,
                "TikTok is a short-form video hosting service owned by the Chinese internet technology company ByteDance. It allows users to create, share, and discover short videos ranging from 15 seconds to 10 minutes.",
                "TikTok was launched in 2016 in China as Douyin. It was released internationally in 2017 after merging with Musical.ly. It quickly became one of the most downloaded apps worldwide, known for its algorithmic feed and viral challenges."));

        KNOWN_APPS.put("FACEBOOK", createKnown("Facebook", "APPLICATION", 100, 75, 75, 100, 100, 85, 80,
                "Facebook is an online social media and social networking service owned by Meta Platforms. It allows users to connect with friends, family, and communities.",
                "Founded in 2004 by Mark Zuckerberg and fellow Harvard College students, Facebook began as a directory for college students. It expanded globally to become the largest social network in the world, with over 3 billion active users."));

        KNOWN_APPS.put("INSTAGRAM", createKnown("Instagram", "APPLICATION", 75, 75, 75, 100, 90, 80, 75,
                "Instagram is a photo and video sharing social networking service owned by Meta Platforms. Users upload media that can be edited with filters and organized by hashtags and geographical tagging.",
                "Created by Kevin Systrom and Mike Krieger, Instagram launched in October 2010. It was acquired by Facebook (now Meta) in April 2012 for approximately US$1 billion. It has since evolved to include Stories and Reels features."));

        KNOWN_APPS.put("X", createKnown("X (Twitter)", "APPLICATION", 100, 25, 25, 100, 80, 60, 50,
                "X, formerly known as Twitter, is a social media platform for real-time microblogging. Users post and interact with short messages known as 'posts' or 'tweets'.",
                "Twitter was created in March 2006. It became a global platform for breaking news and public discourse. In 2022, it was acquired by Elon Musk for $44 billion and subsequently rebranded to X in July 2023."));

        // --- MESSAGING ---
        KNOWN_APPS.put("WHATSAPP", createKnown("WhatsApp", "APPLICATION", 75, 75, 25, 75, 50, 60, 20,
                "WhatsApp is a freeware, cross-platform, centralized instant messaging (IM) and voice-over-IP (VoIP) service owned by Meta Platforms.",
                "Founded in 2009 by Brian Acton and Jan Koum, WhatsApp was acquired by Facebook in 2014 for approx. US$19.3 billion. It is one of the most popular messaging apps globally, known for end-to-end encryption."));

        KNOWN_APPS.put("SIGNAL", createKnown("Signal", "APPLICATION", 0, 0, 0, 0, 0, 10, 0,
                "Signal is an encrypted instant messaging service developed by the non-profit Signal Foundation and Signal Messenger LLC. It uses standard cellular telephone numbers as identifiers.",
                "Signal was launched in 2014, evolving from earlier encrypted voice and text apps RedPhone and TextSecure. Ideally known for its focus on privacy and minimal data collection."));

        // --- SHOPPING ---
        KNOWN_APPS.put("AMAZON", createKnown("Amazon", "APPLICATION", 75, 50, 75, 100, 80, 90, 40,
                "Amazon Shopping allows users to browse, search, and purchase millions of products from Amazon.com. It features personalized recommendations, order tracking, and voice shopping.",
                "Amazon started as an online bookstore in 1994. It has expanded to become the world's largest online marketplace, AI assistant provider, and cloud computing platform."));

        KNOWN_APPS.put("TEMU", createKnown("Temu", "APPLICATION", 100, 75, 75, 100, 100, 95, 90,
                "Temu is an online marketplace operated by PDD Holdings. It offers discounted goods shipped directly from China to consumers worldwide.",
                "Launched in the United States in September 2022, Temu quickly gained popularity due to its extremely low prices and aggressive marketing campaigns, including Super Bowl ads."));

        // --- GOOGLE ---
        KNOWN_APPS.put("GOOGLE", createKnown("Google.com", "WEBSITE", 60, 50, 50, 100, 90, 20, 20,
                "Google Search is a search engine provided by Google. It handles more than 3.5 billion searches per day and has a 92% share of the global search engine market.",
                "Google began in 1996 as a research project by Larry Page and Sergey Brin. It was incorporated in 1998. The company's mission is 'to organize the world's information and make it universally accessible and useful'."));
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

        boolean isUrl = target.startsWith("http") || target.contains(".") || target.startsWith("www");

        // If it is NOT a URL and NOT in our known database (checked previously), return
        // NULL to indicate "Not Found"
        if (!isUrl) {
            return null; // Controller will translate this to 404
        }

        score.setType(isUrl ? "WEBSITE" : "APPLICATION");

        // Generic Description for Unknown Apps
        score.setDescription(
                "This represents a dynamically analyzed application or website. Our heuristic engine has detected potential privacy risks based on simulated network traffic and permission requests.");
        score.setHistory(
                "No historical data available for this specific target. It was analyzed in real-time by the PrivacyRisk engine.");

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
            int retention, int tracking, int permission, int network, String description, String history) {
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
        score.setDescription(description);
        score.setHistory(history);
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
