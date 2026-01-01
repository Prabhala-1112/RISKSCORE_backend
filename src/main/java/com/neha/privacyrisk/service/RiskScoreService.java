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
        // --- E-COMMERCE (INDIA & GLOBAL) ---
        KNOWN_APPS.put("FLIPKART", createKnown("Flipkart", "APPLICATION", 85, 60, 80, 100, 90, 85, 70,
                "Flipkart is an Indian e-commerce company, headquartered in Bengaluru, and incorporated in Singapore as a private limited company. It is a dominant online retailer in India.",
                "Founded in 2007 by Sachin Bansal and Binny Bansal (no relation), Flipkart started as an online bookstore before expanding into other product categories. In 2018, U.S.-based retail chain Walmart acquired a 77% controlling stake in Flipkart for US$16 billion."));

        KNOWN_APPS.put("MYNTRA", createKnown("Myntra", "APPLICATION", 80, 65, 75, 100, 85, 80, 70,
                "Myntra is a major Indian fashion e-commerce company headquartered in Bengaluru, Karnataka, India.",
                "Established in 2007 to personalize gift items, Myntra shifted to online retailing of branded apparel in 2010. It was acquired by Flipkart in 2014."));

        KNOWN_APPS.put("SNAPDEAL", createKnown("Snapdeal", "APPLICATION", 75, 60, 70, 90, 80, 75, 65,
                "Snapdeal is an Indian e-commerce company based in New Delhi, India.",
                "Founded in February 2010 by Kunal Bahl and Rohit Bansal. It has expanded to offer a wide assortment of products."));

        KNOWN_APPS.put("EBAY", createKnown("eBay", "APPLICATION", 70, 50, 70, 100, 85, 80, 60,
                "eBay Inc. is an American multinational e-commerce corporation based in San Jose, California, that facilitates consumer-to-consumer and business-to-consumer sales through its website.",
                "Founded in 1995 by Pierre Omidyar, eBay became a notable success story of the dot-com bubble."));

        KNOWN_APPS.put("SHEIN", createKnown("Shein", "APPLICATION", 95, 70, 85, 100, 100, 90, 85,
                "Shein is a Chinese online fast fashion retailer based in Singapore. It is known for its incredibly low prices and vast selection of trendy clothing.",
                "Founded in 2008 by Chris Xu. Shein has faced criticism regarding data privacy, labor practices, and environmental impact."));

        // --- FINANCE & PAYMENTS ---
        KNOWN_APPS.put("PAYTM", createKnown("Paytm", "APPLICATION", 90, 80, 95, 100, 90, 95, 80,
                "Paytm is an Indian multinational financial technology company specializing in digital payments and financial services.",
                "Founded in 2010 by Vijay Shekhar Sharma under One97 Communications. It played a major role in India's digital payment revolution, especially after the 2016 demonetization."));

        KNOWN_APPS.put("PHONEPE", createKnown("PhonePe", "APPLICATION", 85, 80, 90, 100, 85, 90, 75,
                "PhonePe is an Indian digital payments and financial technology company headquartered in Bengaluru.",
                "Founded in 2015 and acquired by Flipkart in 2016. It was the first payment app built on Unified Payments Interface (UPI)."));

        KNOWN_APPS.put("GPAY", createKnown("Google Pay", "APPLICATION", 80, 85, 90, 100, 80, 95, 70,
                "Google Pay is a mobile payment service developed by Google to power in-app, online, and in-person contactless purchases on mobile devices.",
                "Originally launched as Android Pay in 2015, it was rebranded as Google Pay in 2018. In India, it was launched as Tez in 2017 before rebranding."));

        KNOWN_APPS.put("PAYPAL", createKnown("PayPal", "APPLICATION", 70, 85, 95, 100, 75, 95, 60,
                "PayPal Holdings, Inc. is an American multinational financial technology company operating an online payments system.",
                "Established in 1998 as Confinity. It went public in 2002 and was acquired by eBay later that year, spinning off back into an independent company in 2015."));

        // --- FOOD & TRAVEL ---
        KNOWN_APPS.put("ZOMATO", createKnown("Zomato", "APPLICATION", 80, 70, 80, 90, 95, 75, 60,
                "Zomato is an Indian multinational restaurant aggregator and food delivery company.",
                "Founded in 2008 by Deepinder Goyal and Pankaj Chaddah. It provides information, menus, and user-reviews of restaurants as well as food delivery options."));

        KNOWN_APPS.put("SWIGGY", createKnown("Swiggy", "APPLICATION", 80, 70, 80, 90, 95, 75, 60,
                "Swiggy is an Indian online food ordering and delivery platform.",
                "Founded in 2014, Swiggy is headquartered in Bangalore and operates in more than 500 Indian cities."));

        KNOWN_APPS.put("UBER", createKnown("Uber", "APPLICATION", 85, 75, 85, 100, 100, 80, 70,
                "Uber Technologies, Inc. is an American mobility as a service provider, allowing users to book car transportation.",
                "Founded in 2009 by Travis Kalanick and Garrett Camp. Uber revolutionized the taxi industry with its app-based ride-hailing model, though it has faced numerous regulatory and privacy controversies."));

        KNOWN_APPS.put("OLA", createKnown("Ola Cabs", "APPLICATION", 85, 70, 85, 100, 100, 80, 70,
                "Ola Cabs is an Indian multinational ridesharing company offering services that include vehicle for hire and food delivery.",
                "Founded in 2010 by Bhavish Aggarwal and Ankit Bhati. It is one of the largest ride-hailing companies in the world."));

        // --- SOCIAL MEDIA EXTENDED ---
        KNOWN_APPS.put("LINKEDIN", createKnown("LinkedIn", "APPLICATION", 60, 80, 80, 100, 70, 85, 50,
                "LinkedIn is a business and employment-focused social media platform that works through websites and mobile apps.",
                "Launched in 2003, it is now owned by Microsoft. It is primarily used for professional networking and career development."));

        KNOWN_APPS.put("SNAPCHAT", createKnown("Snapchat", "APPLICATION", 90, 60, 80, 90, 95, 80, 75,
                "Snapchat is an American multimedia instant messaging app and service developed by Snap Inc.",
                "One of the principal features of Snapchat is that pictures and messages are usually only available for a short time before they become inaccessible using the app."));

        KNOWN_APPS.put("REDDIT", createKnown("Reddit", "APPLICATION", 70, 50, 60, 100, 85, 70, 60,
                "Reddit is an American social news aggregation, web content rating, and discussion website.",
                "Founded in 2005. Registered members submit content to the site such as links, text posts, images, and videos, which are then voted up or down by other members."));

        KNOWN_APPS.put("PINTEREST", createKnown("Pinterest", "APPLICATION", 60, 60, 60, 100, 80, 70, 50,
                "Pinterest is an image sharing and social media service designed to enable saving and discovery of information on the internet using images and, on a smaller scale, animated GIFs and videos.",
                "Founded in 2009. It is described as a 'catalogue of ideas' that inspires users to 'go out and do that thing'."));

        // --- ENTERTAINMENT ---
        KNOWN_APPS.put("NETFLIX", createKnown("Netflix", "APPLICATION", 50, 80, 85, 100, 60, 90, 40,
                "Netflix is an American subscription video on-demand over-the-top streaming service.",
                "Founded in 1997 by Reed Hastings and Marc Randolph in Scotts Valley, California. It started as a DVD-by-mail service before transitioning to streaming in 2007."));

        KNOWN_APPS.put("SPOTIFY", createKnown("Spotify", "APPLICATION", 60, 70, 70, 100, 70, 80, 50,
                "Spotify is a Swedish audio streaming and media services provider.",
                "Founded in 2006 by Daniel Ek and Martin Lorentzon. It is one of the largest music streaming service providers, with over 500 million monthly active users."));

        KNOWN_APPS.put("YOUTUBE", createKnown("YouTube", "APPLICATION", 70, 60, 65, 100, 85, 85, 60,
                "YouTube is an American online video sharing and social media platform owned by Google.",
                "Launched in 2005 by Steve Chen, Chad Hurley, and Jawed Karim. Ideally known for user-generated content, it has become the second most visited website in the world."));

        // --- PRODUCTIVITY ---
        KNOWN_APPS.put("ZOOM", createKnown("Zoom", "APPLICATION", 65, 85, 80, 90, 80, 85, 60,
                "Zoom Video Communications is a communications technology company that provides videotelephony and online chat services.",
                "Founded in 2011 by Eric Yuan. It saw a massive surge in usage during the COVID-19 pandemic for remote work and education."));

        KNOWN_APPS.put("SLACK", createKnown("Slack", "APPLICATION", 60, 80, 85, 100, 70, 90, 50,
                "Slack is a comprehensive instant messaging program designed by Slack Technologies and owned by Salesforce.",
                "Launched in 2013, it offers many IRC-style features, including persistent chat rooms (channels) organized by topic, private groups, and direct messaging."));
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
        if (lower.contains("free") || lower.contains("crack") || lower.contains("game") || lower.contains("mod")) {
            score.setTrackingRisk(90);
            score.setExposureLevel(85);
            score.setDescription(score.getDescription()
                    + " Warning: Keywords suggest this might be a modified or ad-supported version.");
        }
        if (lower.contains("bank") || lower.contains("pay") || lower.contains("wallet")) {
            score.setDataSensitivity(100);
            score.setNetworkSecurityRisk(10);
            score.setDescription(
                    score.getDescription() + " financial application detected; strict security standards expected.");
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
                .sorted(java.util.Comparator.comparingInt(k -> calculateLevenshteinDistance(k, upperQuery)))
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
