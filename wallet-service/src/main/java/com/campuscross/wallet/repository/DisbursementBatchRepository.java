package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.DisbursementBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisbursementBatchRepository extends JpaRepository<DisbursementBatch, Long> {
    Optional<DisbursementBatch> findByBatchId(String batchId);
    List<DisbursementBatch> findByCreatedBy(Long createdBy);
    List<DisbursementBatch> findByStatus(String status);
}