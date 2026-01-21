package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.OrderView;

/**
 * Service for sending notifications to users.
 */
public interface NotificationService {

    /**
     * Send order accepted notification to customer.
     * Includes distance and estimated delivery time.
     *
     * @param order The accepted order with delivery metrics
     */
    void notifyOrderAccepted(OrderView order);

    /**
     * Send order rejected notification to customer.
     *
     * @param order The rejected order
     * @param reason Reason for rejection
     */
    void notifyOrderRejected(OrderView order, String reason);

    /**
     * Send order status update notification.
     *
     * @param order The order with updated status
     */
    void notifyOrderStatusUpdate(OrderView order);

    /**
     * Send order completed notification.
     *
     * @param order The completed order
     */
    void notifyOrderCompleted(OrderView order);
}
