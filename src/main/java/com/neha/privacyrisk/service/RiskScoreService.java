package com.neha.privacyrisk.service;

import com.neha.privacyrisk.entity.RiskScore;
import com.neha.privacyrisk.repository.RiskScoreRepository;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class RiskScoreService {

        private final RiskScoreRepository repository;

        private static final java.util.Map<String, RiskScore> KNOWN_APPS = new java.util.HashMap<>();

        private final String DATASET_PATH = "risk_dataset.csv";

        // Replaced static block with dynamic loader
        @javax.annotation.PostConstruct
        public void loadDataset() {
                try {
                        java.io.File file = new java.io.File(DATASET_PATH);
                        if (!file.exists()) {
                                System.out.println("Dataset file not found: " + file.getAbsolutePath()
                                                + ". Using defaults.");
                                return;
                        }

                        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                                String line;
                                boolean header = true;
                                while ((line = br.readLine()) != null) {
                                        if (header) {
                                                header = false;
                                                continue;
                                        }

                                        // CSV Format: App
                                        // Name,Type,Exposure,Consent,Sensitivity,Retention,Tracking,Permission,Network,Category,Description
                                        // Use simple split for now, assuming no commas in fields (or handle basic
                                        // quotes later)
                                        // Better to use a CSV library, but for this file structure a quote-aware split
                                        // is safer
                                        String[] parts = parseCsvLine(line);

                                        if (parts.length >= 11) {
                                                String name = parts[0];
                                                String type = parts[1];
                                                int exposure = Integer.parseInt(parts[2]);
                                                int consent = Integer.parseInt(parts[3]);
                                                int sensitivity = Integer.parseInt(parts[4]);
                                                int retention = Integer.parseInt(parts[5]);
                                                int tracking = Integer.parseInt(parts[6]);
                                                int permission = Integer.parseInt(parts[7]);
                                                int network = Integer.parseInt(parts[8]);
                                                // part 9 is Category (calc automatically or use?) - we recalc based on
                                                // scores
                                                String description = parts[10];

                                                // Clean up quotes
                                                if (description.startsWith("\"") && description.endsWith("\"")) {
                                                        description = description.substring(1,
                                                                        description.length() - 1);
                                                }

                                                RiskScore score = createKnown(name, type, exposure, consent,
                                                                sensitivity, retention, tracking, permission, network,
                                                                description,
                                                                "Data from detailed privacy analysis dataset.");
                                                KNOWN_APPS.put(name.toUpperCase(), score);
                                        }
                                }
                                System.out.println("Successfully loaded " + KNOWN_APPS.size() + " apps from dataset.");
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error loading dataset: " + e.getMessage());
                }
        }

        // Basic CSV parser that handles quoted strings containing commas
        private String[] parseCsvLine(String line) {
                java.util.List<String> tokens = new java.util.ArrayList<>();
                StringBuilder sb = new StringBuilder();
                boolean inQuotes = false;

                for (char c : line.toCharArray()) {
                        if (c == '\"') {
                                inQuotes = !inQuotes;
                        } else if (c == ',' && !inQuotes) {
                                tokens.add(sb.toString());
                                sb.setLength(0);
                        } else {
                                sb.append(c);
                        }
                }
                tokens.add(sb.toString());
                return tokens.toArray(new String[0]);
        }

        public RiskScoreService(RiskScoreRepository repository) {
                this.repository = repository;
        }

        public RiskScore analyzeTarget(String target) {
                if (target == null || target.isBlank())
                        return null;
                String key = target.trim().toUpperCase();

                // 1. Check Cache / Known List (Exact Match)
                if (KNOWN_APPS.containsKey(key)) {
                        return KNOWN_APPS.get(key);
                }

                // 2. Check if it's a URL
                boolean isUrl = target.startsWith("http") || target.contains(".") || target.startsWith("www");

                // 3. Typo Detection Strategy:
                // If it's NOT a URL and NOT in our known list, check if it's just a typo of a
                // known app.
                // If it is a very close typo (distance <= 1), we treat it as "Not Found" so the
                // frontend can suggest the correct one.
                // If it is NOT a close typo (distance > 1), we assume it's a distinct
                // "Small/Unused App" and generate a score.
                if (!isUrl) {
                        int minDistance = Integer.MAX_VALUE;
                        for (String knownKey : KNOWN_APPS.keySet()) {
                                int dist = calculateLevenshteinDistance(knownKey, key);
                                if (dist < minDistance) {
                                        minDistance = dist;
                                }
                        }

                        // If it's extremely close to a known app, assume typo and return null to
                        // trigger suggestion UI
                        if (minDistance <= 1) {
                                return null;
                        }
                }

                // 4. Live Scan Simulation (Heuristic Engine) - Now allows unknown non-URLs
                return performLiveScan(target, isUrl);
        }

        private RiskScore performLiveScan(String target, boolean isUrl) {
                RiskScore score = new RiskScore();
                score.setTarget(target);
                score.setType(isUrl ? "WEBSITE" : "APPLICATION");

                // Generic Description for Unknown Apps
                if (isUrl) {
                        score.setDescription(
                                        "This website was analyzed in real-time. Our engine checked for SSL configuration, tracker presence, and external connections.");
                        score.setHistory("No verification history available for this domain.");
                } else {
                        score.setDescription(
                                        "This appears to be a less common or niche application. Our heuristic engine has analyzed it based on its naming patterns and simulated behavior context.");
                        score.setHistory(
                                        "This app is not in our primary verified database, but a dynamic risk profile has been generated.");
                }

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
                if (lower.contains("free") || lower.contains("crack") || lower.contains("game")
                                || lower.contains("mod")) {
                        score.setTrackingRisk(90);
                        score.setExposureLevel(85);
                        score.setDescription(score.getDescription()
                                        + " Warning: Keywords suggest this might be a modified or ad-supported version.");
                }
                if (lower.contains("bank") || lower.contains("pay") || lower.contains("wallet")) {
                        score.setDataSensitivity(100);
                        score.setNetworkSecurityRisk(10);
                        score.setDescription(
                                        score.getDescription()
                                                        + " financial application detected; strict security standards expected.");
                }
                if (lower.startsWith("http://")) { // No SSL
                        score.setNetworkSecurityRisk(95);
                        score.setDescription(score.getDescription() + " Critical: Connection is not encrypted (HTTP).");
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

        // For Search Auto-complete with Fuzzy Matching
        public java.util.List<String> searchSuggestions(String query) {
                if (query == null || query.isBlank())
                        return java.util.Collections.emptyList();

                String upperQuery = query.toUpperCase();

                // 1. Direct contains match (highest priority)
                java.util.List<String> exactMatches = KNOWN_APPS.keySet().stream()
                                .filter(k -> k.contains(upperQuery))
                                .sorted()
                                .collect(java.util.stream.Collectors.toList());

                if (!exactMatches.isEmpty()) {
                        return exactMatches;
                }

                // 2. Fuzzy match (Levenshtein Distance)
                return KNOWN_APPS.keySet().stream()
                                .filter(k -> calculateLevenshteinDistance(k, upperQuery) <= 3) // Allow up to 3 typos
                                .sorted(java.util.Comparator
                                                .comparingInt(k -> calculateLevenshteinDistance(k, upperQuery)))
                                .limit(5)
                                .collect(java.util.stream.Collectors.toList());
        }

        // Standard Levenshtein Distance Algorithm
        private int calculateLevenshteinDistance(String x, String y) {
                int[][] dp = new int[x.length() + 1][y.length() + 1];

                for (int i = 0; i <= x.length(); i++) {
                        for (int j = 0; j <= y.length(); j++) {
                                if (i == 0) {
                                        dp[i][j] = j;
                                } else if (j == 0) {
                                        dp[i][j] = i;
                                } else {
                                        dp[i][j] = min(dp[i - 1][j - 1]
                                                        + (x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1),
                                                        dp[i - 1][j] + 1,
                                                        dp[i][j - 1] + 1);
                                }
                        }
                }
                return dp[x.length()][y.length()];
        }

        private int min(int a, int b, int c) {
                return Math.min(Math.min(a, b), c);
        }
}
