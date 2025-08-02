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
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRole(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.active = false")
    List<User> findInactiveUsers();

    List<User> findByActiveTrue();
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'SERVICE_PROVIDER'")
    List<User> findAllServiceProviders();
}
