package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.CurrencyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyAccountRepository extends JpaRepository<CurrencyAccount, Long> {
    List<CurrencyAccount> findByWalletId(Long walletId);
    Optional<CurrencyAccount> findByWalletIdAndCurrencyCode(Long walletId, String currencyCode);
}