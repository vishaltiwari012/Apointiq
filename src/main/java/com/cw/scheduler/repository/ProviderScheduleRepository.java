package com.cw.scheduler.repository;

import com.cw.scheduler.entity.ProviderSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderScheduleRepository extends JpaRepository<ProviderSchedule, Long> {

    // Get all schedule entries for a provider
    List<ProviderSchedule> findByProviderId(Long providerId);

    // Get schedule for a provider on a specific day
    Optional<ProviderSchedule> findByProviderIdAndDayOfWeek(Long providerId, DayOfWeek dayOfWeek);

    // Get all providers available on a specific day
    @Query("SELECT ps FROM ProviderSchedule ps WHERE ps.dayOfWeek = :dayOfWeek")
    List<ProviderSchedule> findAllByDayOfWeek(@Param("dayOfWeek") DayOfWeek dayOfWeek);

    // Check if provider is available at a given time
    @Query(" SELECT ps FROM ProviderSchedule ps " +
           "WHERE ps.provider.id = :providerId" +
            " AND ps.dayOfWeek = :dayOfWeek " +
             "AND :time BETWEEN ps.startTime AND ps.endTime")
    Optional<ProviderSchedule> isProviderAvailableAt(
            @Param("providerId") Long providerId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("time") LocalTime time
    );
}
