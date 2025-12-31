package com.neha.privacyrisk.controller;

import com.neha.privacyrisk.entity.RiskScore;
import com.neha.privacyrisk.service.RiskScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scan")
public class RiskScoreController {

    private final RiskScoreService service;

    public RiskScoreController(RiskScoreService service) {
        this.service = service;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String query) {
        return ResponseEntity.ok(service.searchSuggestions(query));
    }

    @PostMapping("/analyze")
    public ResponseEntity<RiskScore> analyzeTarget(@RequestBody Map<String, String> request) {
        String target = request.get("target");
        if (target == null || target.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        RiskScore score = service.analyzeTarget(target);
        return ResponseEntity.ok(score);
    }
}
