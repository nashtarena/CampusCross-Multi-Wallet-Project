package com.campuscross.wallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscross.wallet.entity.DisbursementItem;

@Repository
public interface DisbursementItemRepository extends JpaRepository<DisbursementItem, Long> {
    List<DisbursementItem> findByBatchId(Long batchId);
    List<DisbursementItem> findByStatus(String status);
}