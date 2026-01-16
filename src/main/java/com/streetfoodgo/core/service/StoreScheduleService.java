package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.Store;
import com.streetfoodgo.core.model.StoreSchedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service for managing and validating store schedules.
 */
public interface StoreScheduleService {

    /**
     * Check if a store is currently open based on its schedule.
     *
     * @param store The store to check
     * @return true if the store is currently open
     */
    boolean isStoreOpen(Store store);

    /**
     * Check if a store is open at a specific date/time.
     *
     * @param store The store to check
     * @param dateTime The date/time to check
     * @return true if the store is open at that time
     */
    boolean isStoreOpenAt(Store store, LocalDateTime dateTime);

    /**
     * Get the store's schedule for a specific day.
     *
     * @param storeId The store ID
     * @param dayOfWeek The day of week
     * @return The schedule for that day, if exists
     */
    StoreSchedule getScheduleForDay(Long storeId, DayOfWeek dayOfWeek);

    /**
     * Get all schedules for a store.
     *
     * @param storeId The store ID
     * @return List of schedules
     */
    List<StoreSchedule> getStoreSchedules(Long storeId);

    /**
     * Create or update store schedule.
     *
     * @param storeId The store ID
     * @param dayOfWeek Day of week
     * @param openTime Opening time
     * @param closeTime Closing time
     * @param isOpen Whether the store is open on this day
     * @return The created/updated schedule
     */
    StoreSchedule setSchedule(Long storeId, DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, boolean isOpen);

    /**
     * Get next opening time for a store.
     *
     * @param store The store
     * @return LocalDateTime of next opening, or null if no schedule
     */
    LocalDateTime getNextOpeningTime(Store store);

    /**
     * Get closing time for today.
     *
     * @param store The store
     * @return LocalTime of closing today, or null if closed today
     */
    LocalTime getTodayClosingTime(Store store);
}
