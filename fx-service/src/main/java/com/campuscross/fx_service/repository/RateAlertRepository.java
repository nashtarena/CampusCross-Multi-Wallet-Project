package com.campuscross.fx_service.repository;

import com.campuscross.fx_service.model.RateAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for RateAlert entities.
 * Extends JpaRepository to inherit standard CRUD operations.
 */
public interface RateAlertRepository extends JpaRepository<RateAlert, Long> {

    /**
     * Finds all active alerts for a specific currency pair.
     * This is the core method used by the monitoring service when a new rate
     * arrives.
     * * @param currencyPair The pair (e.g., "USD/EUR") to filter by.
     * 
     * @param status The required status, typically RateAlert.AlertStatus.ACTIVE.
     * @return A list of matching RateAlert entities.
     */
    List<RateAlert> findAllByCurrencyPairAndStatus(String currencyPair, RateAlert.AlertStatus status);

    /**
     * Fetches all alerts belonging to a specific user.
     * Used by the RateAlertController for the frontend display.
     * * @param userId The ID of the user.
     * 
     * @return A list of RateAlerts set by the user.
     */
    List<RateAlert> findAllByUserId(Long userId);
}