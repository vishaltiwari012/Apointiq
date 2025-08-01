package com.cw.scheduler.repository;

import com.cw.scheduler.entity.Category;
import com.cw.scheduler.entity.OfferedService;
import com.cw.scheduler.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferedServiceRepository extends JpaRepository<OfferedService, Long> {
    List<OfferedService> findByCategory(Category category);

    List<OfferedService> findByProvider(ServiceProvider provider);
    int countByProvider(ServiceProvider provider);

    List<OfferedService> findByNameContainingIgnoreCase(String name);

    // Check if a provider offers a specific service
    Optional<OfferedService> findByIdAndProviderId(Long serviceId, Long providerId);
}
