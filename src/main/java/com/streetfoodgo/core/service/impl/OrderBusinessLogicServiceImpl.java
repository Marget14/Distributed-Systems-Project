package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.model.PersonType;
import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.port.SmsNotificationPort;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.repository.OrderRepository;
import com.streetfoodgo.core.security.CurrentUser;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.OrderBusinessLogicService;

import com.streetfoodgo.core.service.mapper.OrderMapper;
import com.streetfoodgo.core.service.model.CompleteOrderRequest;
import com.streetfoodgo.core.service.model.OpenOrderRequest;
import com.streetfoodgo.core.service.model.StartOrderRequest;
import com.streetfoodgo.core.service.model.OrderView;

import jakarta.persistence.EntityNotFoundException;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link OrderBusinessLogicService}.
 *
 * <p>
 * TODO some parts can be reused (e.g., security checks)
 */
@Service
public class OrderBusinessLogicServiceImpl implements OrderBusinessLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBusinessLogicServiceImpl.class);

    private static final Set<OrderStatus> ACTIVE = Set.of(OrderStatus.QUEUED, OrderStatus.IN_PROGRESS);

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SmsNotificationPort smsNotificationPort;

    public OrderBusinessLogicServiceImpl(final OrderMapper orderMapper,
                                          final OrderRepository orderRepository,
                                          final PersonRepository personRepository,
                                          final CurrentUserProvider currentUserProvider,
                                          final SmsNotificationPort smsNotificationPort) {
        if (orderMapper == null) throw new NullPointerException();
        if (orderRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();

        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.personRepository = personRepository;
        this.currentUserProvider = currentUserProvider;
        this.smsNotificationPort = smsNotificationPort;
    }

    private void notifyPerson(final OrderView orderView, final PersonType type) {
        final String e164;
        if (type == PersonType.WAITER) {
            e164 = orderView.waiter().mobilePhoneNumber();
        } else if (type == PersonType.CUSTOMER) {
            e164 = orderView.customer().mobilePhoneNumber();
        } else {
            throw new RuntimeException("Unreachable");
        }
        final String content = String.format("Order %s new status: %s", orderView.id(), orderView.status().name());
        final boolean sent = this.smsNotificationPort.sendSms(e164, content);
        if (!sent) {
            LOGGER.warn("SMS send to {} failed", e164);
        }
    }

    @Override
    public Optional<OrderView> getOrder(final Long id) {
        if (id == null) throw new NullPointerException();
        if (id <= 0) throw new IllegalArgumentException();

        // --------------------------------------------------

        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();

        // --------------------------------------------------

        final Order order;
        try {
            order = this.orderRepository.getReferenceById(id);
        } catch (EntityNotFoundException ignored) {
            return Optional.empty();
        }

        // User MUST have access to Ticket.
        // --------------------------------------------------

        final long orderPersonId;
        if (currentUser.type() == PersonType.WAITER) {
            orderPersonId = order.getWaiter().getId();
        } else if (currentUser.type() == PersonType.CUSTOMER) {
            orderPersonId = order.getCustomer().getId();
        } else {
            throw new SecurityException("unsupported PersonType");
        }
        if (currentUser.id() != orderPersonId) {
            return Optional.empty(); // this Order does not exist for this user.
        }

        // --------------------------------------------------

        final OrderView orderView = this.orderMapper.convertOrderToOrderView(order);

        // --------------------------------------------------

        return Optional.of(orderView);
    }

    @Override
    public List<OrderView> getOrders() {
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        final List<Order> orderList;
        if (currentUser.type() == PersonType.WAITER) {
            orderList = this.orderRepository.findAllByWaiterId(currentUser.id());
        } else if (currentUser.type() == PersonType.CUSTOMER) {
            orderList = this.orderRepository.findAllByCustomerId(currentUser.id());
        } else {
            throw new SecurityException("unsupported PersonType");
        }
        return orderList.stream()
                .map(this.orderMapper::convertOrderToOrderView)
                .toList();
    }

    @Transactional
    @Override
    public OrderView openOrder(@Valid final OpenOrderRequest openOrderRequest, final boolean notify) {
        if (openOrderRequest == null) throw new NullPointerException();

        // Unpack.
        // --------------------------------------------------

        final long customerId = openOrderRequest.customerId();
        final long waiterId = openOrderRequest.waiterId();
        final String subject = openOrderRequest.subject();
        final String customerContent = openOrderRequest.customerContent();

        // --------------------------------------------------

        final Person customer = this.personRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("customer not found"));
        final Person waiter = this.personRepository.findById(waiterId)
                .orElseThrow(() -> new IllegalArgumentException("waiter not found"));

        // --------------------------------------------------

        if (customer.getType() != PersonType.CUSTOMER) {
            throw new IllegalArgumentException("customerId must refer to a CUSTOMER");
        }
        if (waiter.getType() != PersonType.WAITER) {
            throw new IllegalArgumentException("waiterId must refer to a WAITER");
        }

        // Security
        // --------------------------------------------------

        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.CUSTOMER) {
            throw new SecurityException("Customer type/role required");
        }
        if (currentUser.id() != customerId) {
            throw new SecurityException("Authenticated customer does not match the ticket's customerId");
        }

        // Rules
        // --------------------------------------------------

        // Rule 1: customer may have at most one active ticket with this waiter.
        if (this.orderRepository.existsByCustomerIdAndWaiterIdAndStatusIn(customerId, waiterId, ACTIVE)) {
            throw new RuntimeException("Customer already has an active ticket with this waiter");
        }

        // Rule 2: customer can open max 4 active tickets in total.
        final long activeCount = this.orderRepository.countByCustomerIdAndStatusIn(customerId, ACTIVE);
        if (activeCount >= 4) {
            throw new RuntimeException("Customer has reached the limit of 4 active tickets");
        }

        // --------------------------------------------------

        Order order = new Order();
        // order.setId(); // auto-generated
        order.setCustomer(customer);
        order.setWaiter(waiter);
        order.setStatus(OrderStatus.QUEUED);
        order.setSubject(subject);
        order.setCustomerContent(customerContent);
        order.setQueuedAt(Instant.now());
        order = this.orderRepository.save(order);

        // --------------------------------------------------

        final OrderView orderView = this.orderMapper.convertOrderToOrderView(order);

        // --------------------------------------------------

        if (notify) {
            this.notifyPerson(orderView, PersonType.WAITER);
        }

        // --------------------------------------------------

        return orderView;
    }

    @Transactional
    @Override
    public OrderView startOrder(@Valid final StartOrderRequest startOrderRequest) {
        if (startOrderRequest == null) throw new NullPointerException();

        // Unpack.
        // --------------------------------------------------

        final long orderId = startOrderRequest.id();

        // --------------------------------------------------

        final Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order does not exist"));

        // Security.
        // --------------------------------------------------

        final long waiterId = order.getWaiter().getId();
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.WAITER) {
            throw new SecurityException("Waiter type/role required");
        }
        if (currentUser.id() != waiterId) {
            throw new SecurityException("Authenticated waiter does not match the order's waiterId");
        }

        // Rules.
        // --------------------------------------------------

        if (order.getStatus() != OrderStatus.QUEUED) {
            throw new IllegalArgumentException("Only QUEUED orders can be started");
        }

        // --------------------------------------------------

        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setInProgressAt(Instant.now());

        // --------------------------------------------------

        final Order savedOrder = this.orderRepository.save(order);

        // --------------------------------------------------

        final OrderView orderView = this.orderMapper.convertOrderToOrderView(savedOrder);

        // --------------------------------------------------

        this.notifyPerson(orderView, PersonType.CUSTOMER);

        // --------------------------------------------------

        return orderView;
    }

    @Transactional
    @Override
    public OrderView completeOrder(@Valid final CompleteOrderRequest completeOrderRequest) {
        if (completeOrderRequest == null) throw new NullPointerException();

        // Unpack.
        // --------------------------------------------------

        final long orderId = completeOrderRequest.id();
        final String waiterContent = completeOrderRequest.waiterContent();

        // --------------------------------------------------

        final Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order does not exist"));

        // Security
        // --------------------------------------------------

        final long waiterId = order.getWaiter().getId();
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.WAITER) {
            throw new SecurityException("Waiter role/type required");
        }
        if (currentUser.id() != waiterId) {
            throw new SecurityException("Authenticated waiter does not match the order's waiterId");
        }

        // Rules
        // --------------------------------------------------

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only IN_PROGRESS orders can be completed");
        }

        // --------------------------------------------------

        order.setStatus(OrderStatus.COMPLETED);
        order.setWaiterContent(waiterContent);
        order.setCompletedAt(Instant.now());

        // --------------------------------------------------

        final Order savedOrder = this.orderRepository.save(order);

        // --------------------------------------------------

        final OrderView orderView = this.orderMapper.convertOrderToOrderView(savedOrder);

        // --------------------------------------------------

        this.notifyPerson(orderView, PersonType.CUSTOMER);

        // --------------------------------------------------

        return orderView;
    }
}

