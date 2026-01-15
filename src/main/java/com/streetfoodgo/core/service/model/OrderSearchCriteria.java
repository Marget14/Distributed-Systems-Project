package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.model.OrderType;

import java.time.Instant;

/**
 * Search criteria for filtering orders.
 */
public record OrderSearchCriteria(
        OrderStatus status,
        OrderType orderType,
        Instant startDate,
        Instant endDate,
        Long storeId,
        Long customerId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OrderStatus status;
        private OrderType orderType;
        private Instant startDate;
        private Instant endDate;
        private Long storeId;
        private Long customerId;

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder orderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder startDate(Instant startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(Instant endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder storeId(Long storeId) {
            this.storeId = storeId;
            return this;
        }

        public Builder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public OrderSearchCriteria build() {
            return new OrderSearchCriteria(status, orderType, startDate, endDate, storeId, customerId);
        }
    }
}
