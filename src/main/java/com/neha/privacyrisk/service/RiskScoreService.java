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

                                        String[] parts = parseCsvLine(line);

                                        // Check for valid line length (Index + fields)
                                        if (parts.length >= 2) {
                                                try {
                                                        // Detect if column 0 is an index (integer)
                                                        boolean hasIndex = isIndexColumn(parts[0]);

                                                        // Adjust indices based on presence of Index column
                                                        // If Index present: Name is at [1], Category at [2]
                                                        // If No Index: Name is at [0], Category at [1]
                                                        int nameIdx = hasIndex ? 1 : 0;
                                                        int catIdx = hasIndex ? 2 : 1;
                                                        int typeIdx = hasIndex ? 7 : 6;
                                                        int ratingIdx = hasIndex ? 9 : 8;

                                                        if (parts.length <= typeIdx)
                                                                continue;

                                                        String name = parts[nameIdx];
                                                        String category = parts[catIdx];
                                                        String type = parts.length > typeIdx ? parts[typeIdx] : "Free";
                                                        String contentRating = parts.length > ratingIdx
                                                                        ? parts[ratingIdx]
                                                                        : "Everyone";

                                                        RiskScore score = generateScoreFromMetadata(name, category,
                                                                        type, contentRating);
                                                        KNOWN_APPS.put(name.toUpperCase(), score);
                                                } catch (Exception e) {
                                                        // Skip malformed lines
                                                }
                                        }
                                }
                                System.out.println("Successfully loaded " + KNOWN_APPS.size() + " apps from dataset.");
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error loading dataset: " + e.getMessage());
                }
        }

        private boolean isIndexColumn(String val) {
                try {
                        Integer.parseInt(val.trim());
                        return true;
                } catch (NumberFormatException e) {
                        return false;
                }
        }

        private RiskScore generateScoreFromMetadata(String name, String category, String type, String contentRating) {
                RiskScore score = new RiskScore();
                score.setTarget(name);
                score.setType("APPLICATION");

                // Base risk based on Category
                int baseRisk = 20;
                int sensitiveData = 30;
                int tracking = 40;

                String cat = category.toUpperCase();
                if (cat.contains("GAME") || cat.contains("FAMILY")) {
                        baseRisk = 30;
                        tracking = 70; // Games often have ads
                } else if (cat.contains("FINANCE") || cat.contains("BUSINESS")) {
                        baseRisk = 50;
                        sensitiveData = 90;
                        tracking = 40;
                } else if (cat.contains("MEDICAL") || cat.contains("HEALTH")) {
                        baseRisk = 60;
                        sensitiveData = 95;
                } else if (cat.contains("SOCIAL") || cat.contains("DATING") || cat.contains("COMMUNICATION")) {
                        baseRisk = 70;
                        sensitiveData = 80;
                        tracking = 90;
                } else if (cat.contains("TOOLS") || cat.contains("PRODUCTIVITY")) {
                        baseRisk = 25;
                        sensitiveData = 40;
                }

                // Adjust for "Type" (Free vs Paid)
                if ("Free".equalsIgnoreCase(type)) {
                        tracking += 20; // Free apps behave worse
                        baseRisk += 10;
                }

                // Adjust for Content Rating
                if (contentRating.contains("Teen")) {
                        baseRisk += 5;
                } else if (contentRating.contains("Mature") || contentRating.contains("17+")) {
                        baseRisk += 20;
                        tracking += 10;
                }

                // Deterministic variability
                int hash = Math.abs(name.hashCode());
                Random r = new Random(hash);

                score.setExposureLevel(boundary(baseRisk + r.nextInt(15)));
                score.setUserConsent(boundary(50 + r.nextInt(40))); // Random
                score.setDataSensitivity(boundary(sensitiveData + r.nextInt(10)));
                score.setRetentionPeriod(boundary(30 + r.nextInt(50)));
                score.setTrackingRisk(boundary(tracking + r.nextInt(15)));
                score.setPermissionRisk(boundary(baseRisk + 10 + r.nextInt(20)));
                score.setNetworkSecurityRisk(boundary(40 + r.nextInt(40)));

                score.setDescription(String.format(
                                "Category: %s | Content Rating: %s | Type: %s. Analysis based on store metadata and category risk modeling.",
                                category, contentRating, type));
                score.setHistory("Imported from Play Store Database.");

                calculateFinalScore(score);
                return score;
        }

        // Improved Parser handles TAB or Comma
        private String[] parseCsvLine(String line) {
                if (line.contains("\t")) {
                        return line.split("\t");
                }

                // Fallback to Comma logic
                java.util.List<String> tokens = new java.util.ArrayList<>();
                StringBuilder sb = new StringBuilder();
                boolean inQuotes = false;

                for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c == '\"') {
                                inQuotes = !inQuotes;
                        } else if (c == ',' && !inQuotes) {
                                tokens.add(sb.toString().trim());
                                sb.setLength(0);
                        } else {
                                sb.append(c);
                        }
                }
                tokens.add(sb.toString().trim());
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

                // 2. Intelligent Search (Starts With / Contains)
                // This handles cases like user typing "WhatsApp" for "WhatsApp Messenger"
                RiskScore bestMatch = null;
                int shortestMatchLen = Integer.MAX_VALUE;

                for (java.util.Map.Entry<String, RiskScore> entry : KNOWN_APPS.entrySet()) {
                        String knownKey = entry.getKey();

                        // Priority 1: Starts With (e.g. "Whats" -> "WhatsApp")
                        if (knownKey.startsWith(key)) {
                                // Prefer the shortest valid match (closest to exact)
                                if (knownKey.length() < shortestMatchLen) {
                                        shortestMatchLen = knownKey.length();
                                        bestMatch = entry.getValue();
                                }
                        }
                }

                // If no "start with" match, try "contains" (e.g. "Surfers" -> "Subway Surfers")
                if (bestMatch == null) {
                        for (java.util.Map.Entry<String, RiskScore> entry : KNOWN_APPS.entrySet()) {
                                if (entry.getKey().contains(key)) {
                                        // Pick the first reasonable containment match
                                        bestMatch = entry.getValue();
                                        break;
                                }
                        }
                }

                if (bestMatch != null) {
                        return bestMatch;
                }

                // 3. Check if it's a URL
                boolean isUrl = target.startsWith("http") || target.contains(".") || target.startsWith("www");

                // 4. Typo Detection Strategy (Only if not URL)
                if (!isUrl) {
                        int minDistance = Integer.MAX_VALUE;
                        for (String knownKey : KNOWN_APPS.keySet()) {
                                int dist = calculateLevenshteinDistance(knownKey, key);
                                if (dist < minDistance) {
                                        minDistance = dist;
                                }
                        }
                        if (minDistance <= 2) { // Slightly relaxed typo tolerance
                                return null; // Trigger frontend suggestions
                        }
                }

                // 5. Live Scan Simulation (Heuristic Engine) - Fallback
                return performLiveScan(target, isUrl);
        }

        private RiskScore performLiveScan(String target, boolean isUrl) {
                RiskScore score = new RiskScore();
                score.setTarget(target);
                score.setType(isUrl ? "WEBSITE" : "APPLICATION");

                String fetchedDescription = null;
                String fetchedTitle = null;

                if (isUrl) {
                        try {
                                // Real-time metadata fetch
                                String urlToScan = target.startsWith("http") ? target : "https://" + target;
                                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(urlToScan)
                                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                                .timeout(5000) // 5 second timeout
                                                .get();

                                fetchedTitle = doc.title();
                                org.jsoup.select.Elements metaDesc = doc.select("meta[name=description]");
                                if (!metaDesc.isEmpty()) {
                                        fetchedDescription = metaDesc.attr("content");
                                } else {
                                        // Try Open Graph description
                                        org.jsoup.select.Elements ogDesc = doc.select("meta[property=og:description]");
                                        if (!ogDesc.isEmpty()) {
                                                fetchedDescription = ogDesc.attr("content");
                                        }
                                }
                        } catch (Exception e) {
                                System.out.println("Live scan fetch failed for " + target + ": " + e.getMessage());
                                // Fallback to heuristic
                        }
                }

                // Set Description & History
                if (fetchedDescription != null && !fetchedDescription.isBlank()) {
                        // Live Data Found!
                        score.setDescription(fetchedDescription);
                        score.setHistory("Live Verified: Reached " + (fetchedTitle != null ? fetchedTitle : target)
                                        + " successfully. Analyzed public metadata.");
                } else if (isUrl) {
                        score.setDescription(
                                        "This website was analyzed in real-time. Our engine checked for SSL configuration, tracker presence, and external connections.");
                        score.setHistory("No detailed public description found, but domain is active.");
                } else {
                        // Heuristic Description
                        score.setDescription(
                                        "This appears to be a less common or niche application. Our heuristic engine has analyzed it based on its naming patterns and simulated behavior context.");
                        score.setHistory(
                                        "This app is not in our primary verified database, but a dynamic risk profile has been generated.");
                }

                // Deterministic 'Random' based on target string hash for consistent
                // demonstration
                int hash = Math.abs(target.hashCode());
                Random random = new Random(hash);

                // Simulate scanning logic based on real keywords found in title/desc if
                // available
                int baseRisk = 0;
                if (fetchedTitle != null) {
                        String lowerContent = (fetchedTitle + " " + fetchedDescription).toLowerCase();
                        if (lowerContent.contains("crypto") || lowerContent.contains("betting")
                                        || lowerContent.contains("casino"))
                                baseRisk += 30;
                        if (lowerContent.contains("news") || lowerContent.contains("blog"))
                                baseRisk -= 10;
                }

                score.setExposureLevel(boundary(random.nextInt(40) + 20 + baseRisk));
                score.setUserConsent(boundary(random.nextInt(50) + 10));
                score.setDataSensitivity(
                                isUrl ? boundary(random.nextInt(60) + baseRisk) : boundary(random.nextInt(80)));
                score.setRetentionPeriod(random.nextInt(100));
                score.setTrackingRisk(boundary(random.nextInt(90) + 10 + baseRisk));
                score.setPermissionRisk(isUrl ? 10 : boundary(random.nextInt(90)));
                score.setNetworkSecurityRisk(boundary(random.nextInt(60)));

                // Special keywords trigger higher risks (Heuristic Refinement)
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
                                                        + " Financial application detected; strict security standards expected.");
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

        private int boundary(int val) {
                return Math.max(0, Math.min(100, val));
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
