package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.service.NotificationService;
import com.streetfoodgo.core.service.model.OrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of NotificationService.
 * Currently logs notifications - can be extended with email/SMS/push notifications.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void notifyOrderAccepted(OrderView order) {
        if (order == null || order.customer() == null) {
            return;
        }

        String message = String.format(
                "✅ Your order #%d has been accepted! " +
                "Distance: %.2f km | Estimated delivery: %d minutes",
                order.id(),
                order.estimatedDeliveryDistanceKm(),
                order.estimatedDeliveryMinutes()
        );

        logNotification(order.customer().emailAddress(), message);
        // TODO: Send email/SMS/push notification
    }

    @Override
    public void notifyOrderRejected(OrderView order, String reason) {
        if (order == null || order.customer() == null) {
            return;
        }

        String message = String.format(
                "❌ Your order #%d has been rejected. " +
                "Reason: %s",
                order.id(),
                reason
        );

        logNotification(order.customer().emailAddress(), message);
        // TODO: Send email/SMS/push notification
    }

    @Override
    public void notifyOrderStatusUpdate(OrderView order) {
        if (order == null || order.customer() == null) {
            return;
        }

        String message = String.format(
                "🔄 Order #%d status updated to: %s",
                order.id(),
                order.status()
        );

        logNotification(order.customer().emailAddress(), message);
        // TODO: Send email/SMS/push notification
    }

    @Override
    public void notifyOrderCompleted(OrderView order) {
        if (order == null || order.customer() == null) {
            return;
        }

        String message = String.format(
                "🎉 Order #%d has been completed! Thank you for your order.",
                order.id()
        );

        logNotification(order.customer().emailAddress(), message);
        // TODO: Send email/SMS/push notification
    }

    private void logNotification(String recipient, String message) {
        LOGGER.info("📧 Notification to {}: {}", recipient, message);
    }
}
