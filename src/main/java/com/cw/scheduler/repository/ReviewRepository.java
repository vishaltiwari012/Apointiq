package com.cw.scheduler.repository;

import com.cw.scheduler.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByServiceId(Long serviceId);
    List<Review> findByUserId(Long userId);
}
