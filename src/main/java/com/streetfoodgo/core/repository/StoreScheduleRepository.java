package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.StoreSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StoreSchedule entity.
 */
@Repository
public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {

    List<StoreSchedule> findByStoreIdOrderByDayOfWeekAsc(Long storeId);

    Optional<StoreSchedule> findByStoreIdAndDayOfWeek(Long storeId, DayOfWeek dayOfWeek);

    void deleteByStoreId(Long storeId);
}
