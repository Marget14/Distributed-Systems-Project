package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.Store;
import com.streetfoodgo.core.model.StoreSchedule;
import com.streetfoodgo.core.repository.StoreRepository;
import com.streetfoodgo.core.repository.StoreScheduleRepository;
import com.streetfoodgo.core.service.StoreScheduleService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of StoreScheduleService.
 */
@Service
public class StoreScheduleServiceImpl implements StoreScheduleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreScheduleServiceImpl.class);

    private final StoreScheduleRepository scheduleRepository;
    private final StoreRepository storeRepository;

    public StoreScheduleServiceImpl(
            final StoreScheduleRepository scheduleRepository,
            final StoreRepository storeRepository) {

        if (scheduleRepository == null) throw new NullPointerException();
        if (storeRepository == null) throw new NullPointerException();

        this.scheduleRepository = scheduleRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public boolean isStoreOpen(final Store store) {
        if (store == null) throw new NullPointerException();
        return isStoreOpenAt(store, LocalDateTime.now());
    }

    @Override
    public boolean isStoreOpenAt(final Store store, final LocalDateTime dateTime) {
        if (store == null) throw new NullPointerException();
        if (dateTime == null) throw new NullPointerException();

        // First check if store is manually set to open/closed
        if (store.getIsOpen() == null || !store.getIsOpen()) {
            LOGGER.debug("Store {} is manually closed", store.getId());
            return false;
        }

        // Get schedule for the day
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        Optional<StoreSchedule> scheduleOpt = scheduleRepository.findByStoreIdAndDayOfWeek(
                store.getId(), dayOfWeek);

        if (scheduleOpt.isEmpty()) {
            LOGGER.debug("No schedule found for store {} on {}", store.getId(), dayOfWeek);
            // If no schedule exists, assume open if manually set to open
            return true;
        }

        StoreSchedule schedule = scheduleOpt.get();
        if (!schedule.getIsOpen()) {
            LOGGER.debug("Store {} is closed on {}", store.getId(), dayOfWeek);
            return false;
        }

        LocalTime currentTime = dateTime.toLocalTime();
        LocalTime openTime = schedule.getOpenTime();
        LocalTime closeTime = schedule.getCloseTime();

        boolean isWithinHours;
        if (closeTime.isBefore(openTime)) {
            // Handles overnight schedules (e.g., 22:00 - 02:00)
            isWithinHours = currentTime.isAfter(openTime) || currentTime.isBefore(closeTime);
        } else {
            // Normal schedule (e.g., 09:00 - 21:00)
            isWithinHours = !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);
        }

        LOGGER.debug("Store {} open check at {}: {} (hours: {} - {})",
                store.getId(), currentTime, isWithinHours, openTime, closeTime);

        return isWithinHours;
    }

    @Override
    public StoreSchedule getScheduleForDay(final Long storeId, final DayOfWeek dayOfWeek) {
        if (storeId == null) throw new IllegalArgumentException();
        if (dayOfWeek == null) throw new IllegalArgumentException();

        return scheduleRepository.findByStoreIdAndDayOfWeek(storeId, dayOfWeek)
                .orElse(null);
    }

    @Override
    public List<StoreSchedule> getStoreSchedules(final Long storeId) {
        if (storeId == null) throw new IllegalArgumentException();

        return scheduleRepository.findByStoreIdOrderByDayOfWeekAsc(storeId);
    }

    @Transactional
    @Override
    public StoreSchedule setSchedule(final Long storeId, final DayOfWeek dayOfWeek,
                                      final LocalTime openTime, final LocalTime closeTime,
                                      final boolean isOpen) {
        if (storeId == null) throw new IllegalArgumentException();
        if (dayOfWeek == null) throw new IllegalArgumentException();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Find existing or create new
        StoreSchedule schedule = scheduleRepository.findByStoreIdAndDayOfWeek(storeId, dayOfWeek)
                .orElse(new StoreSchedule());

        schedule.setStore(store);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setOpenTime(openTime);
        schedule.setCloseTime(closeTime);
        schedule.setIsOpen(isOpen);

        schedule = scheduleRepository.save(schedule);

        LOGGER.info("Schedule set for store {} on {}: {} ({} - {})",
                storeId, dayOfWeek, isOpen ? "OPEN" : "CLOSED", openTime, closeTime);

        return schedule;
    }

    @Override
    public LocalDateTime getNextOpeningTime(final Store store) {
        if (store == null) throw new NullPointerException();

        if (!store.getIsOpen()) {
            return null; // Store is manually closed
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkTime = now;

        // Check next 7 days
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = checkTime.getDayOfWeek();
            Optional<StoreSchedule> scheduleOpt = scheduleRepository.findByStoreIdAndDayOfWeek(
                    store.getId(), day);

            if (scheduleOpt.isPresent() && scheduleOpt.get().getIsOpen()) {
                StoreSchedule schedule = scheduleOpt.get();
                LocalTime openTime = schedule.getOpenTime();

                // If today and not yet open time
                if (i == 0 && checkTime.toLocalTime().isBefore(openTime)) {
                    return LocalDateTime.of(checkTime.toLocalDate(), openTime);
                }

                // If not today
                if (i > 0) {
                    return LocalDateTime.of(checkTime.toLocalDate(), openTime);
                }
            }

            checkTime = checkTime.plusDays(1);
        }

        return null; // No opening time found in next 7 days
    }

    @Override
    public LocalTime getTodayClosingTime(final Store store) {
        if (store == null) throw new NullPointerException();

        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        Optional<StoreSchedule> scheduleOpt = scheduleRepository.findByStoreIdAndDayOfWeek(
                store.getId(), today);

        if (scheduleOpt.isPresent() && scheduleOpt.get().getIsOpen()) {
            return scheduleOpt.get().getCloseTime();
        }

        return null;
    }
}
