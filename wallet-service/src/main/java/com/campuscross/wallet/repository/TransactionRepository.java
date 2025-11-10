package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceId(String referenceId);
    List<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);
}