package com.neha.privacyrisk.repository;

import com.neha.privacyrisk.entity.RiskScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {
}
