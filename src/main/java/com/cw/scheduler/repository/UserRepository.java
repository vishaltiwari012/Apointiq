package com.cw.scheduler.repository;

import com.cw.scheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Login with phone number or email
    Optional<User> findByPhoneNumberOrEmail(String phoneNumber, String email);

    // Check existence
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByPhoneNumber(String phoneNumber);

    // Get users by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRole(@Param("roleName") String roleName);

    // ✅ Soft-deleted users (if needed)
    @Query("SELECT u FROM User u WHERE u.active = false")
    List<User> findInactiveUsers();

    // Get all providers
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'PROVIDER'")
    List<User> findAllProviders();
}
