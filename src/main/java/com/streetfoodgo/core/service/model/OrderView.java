package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.service.OrderBusinessLogicService;

import java.time.Instant;

/**
 * General view of {@link com.streetfoodgo.core.model.Order} entity.
 *
 * @see com.streetfoodgo.core.model.Order
 * @see OrderBusinessLogicService
 */
public record OrderView(
    long id,
    PersonView customer,
    PersonView waiter,
    OrderStatus status,
    String subject,
    String customerContent,
    String waiterContent,
    Instant queuedAt,
    Instant inProgressAt,
    Instant completedAt
) {}
