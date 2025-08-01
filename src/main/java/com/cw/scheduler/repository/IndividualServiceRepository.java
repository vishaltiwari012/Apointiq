package com.cw.scheduler.repository;

import com.cw.scheduler.entity.IndividualService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndividualServiceRepository extends JpaRepository<IndividualService, Long> {
    List<IndividualService> findByOfferedService_Id(Long offeredServiceId);
}
