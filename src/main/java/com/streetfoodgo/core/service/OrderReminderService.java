package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.port.SmsNotificationPort;
import com.streetfoodgo.core.repository.OrderRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for managing order reminders.
 */
@Service
public class OrderReminderService {

    private final OrderRepository orderRepository;
    private final SmsNotificationPort smsNotificationPort;

    public OrderReminderService(final OrderRepository orderRepository,
                                 final SmsNotificationPort smsNotificationPort) {
        if (orderRepository == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();

        this.orderRepository = orderRepository;
        this.smsNotificationPort = smsNotificationPort;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void remindTeacherOfStaleQueuedOrders() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.DAYS);
        final List<Order> orderList = this.orderRepository.findByStatusAndQueuedAtBefore(OrderStatus.QUEUED, cutoff);
        for (final Order order : orderList) {
            final String e164 = order.getWaiter().getMobilePhoneNumber();
            final String content = String.format("Reminder: Order %s QUEUED", order.getId());
            this.smsNotificationPort.sendSms(e164, content);
        }
    }
}
