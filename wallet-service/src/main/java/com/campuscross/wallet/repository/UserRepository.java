package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    Optional<User> findByStudentId(String studentId);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByStudentId(String studentId);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByStatus(User.UserStatus status);
    
    List<User> findByCampusName(String campusName);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :threshold")
    List<User> findUnverifiedUsersCreatedBefore(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :threshold AND u.status = 'ACTIVE'")
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);
}
