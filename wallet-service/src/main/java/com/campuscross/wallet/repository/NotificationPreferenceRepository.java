package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, String> {
    Optional<NotificationPreference> findByUserId(String userId);
}
