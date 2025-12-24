package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.security.CurrentUser;
import com.streetfoodgo.core.service.model.CompleteOrderRequest;
import com.streetfoodgo.core.service.model.OpenOrderRequest;
import com.streetfoodgo.core.service.model.StartOrderRequest;
import com.streetfoodgo.core.service.model.OrderView;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link Order}.
 *
 * <p><strong>All methods MUST be {@link CurrentUser}-aware.</strong></p>
 */
public interface OrderBusinessLogicService {

    Optional<OrderView> getOrder(final Long id);

    List<OrderView> getOrders();

    OrderView openOrder(final OpenOrderRequest openOrderRequest, final boolean notify);

    default OrderView openOrder(final OpenOrderRequest openOrderRequest) {
        return this.openOrder(openOrderRequest, true);
    }

    OrderView startOrder(final StartOrderRequest startOrderRequest);

    OrderView completeOrder(final CompleteOrderRequest completeOrderRequest);
}
