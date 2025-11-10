package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.RiskScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {
    List<RiskScore> findByUserId(Long userId);
    List<RiskScore> findByStatus(String status);
    List<RiskScore> findByRiskLevel(String riskLevel);
}