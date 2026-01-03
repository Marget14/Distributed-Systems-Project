package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.*;
import com.streetfoodgo.core.port.SmsNotificationPort;
import com.streetfoodgo.core.repository.*;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.mapper.OrderMapper;
import com.streetfoodgo.core.service.model.*;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of OrderService for StreetFoodGo.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final PersonRepository personRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderMapper orderMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SmsNotificationPort smsNotificationPort;

    public OrderServiceImpl(
            final OrderRepository orderRepository,
            final StoreRepository storeRepository,
            final PersonRepository personRepository,
            final DeliveryAddressRepository deliveryAddressRepository,
            final MenuItemRepository menuItemRepository,
            final OrderMapper orderMapper,
            final CurrentUserProvider currentUserProvider,
            final SmsNotificationPort smsNotificationPort) {

        if (orderRepository == null) throw new NullPointerException();
        if (storeRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (deliveryAddressRepository == null) throw new NullPointerException();
        if (menuItemRepository == null) throw new NullPointerException();
        if (orderMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();

        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.personRepository = personRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderMapper = orderMapper;
        this.currentUserProvider = currentUserProvider;
        this.smsNotificationPort = smsNotificationPort;
    }

    @Transactional
    @Override
    public OrderView createOrder(final CreateOrderRequest request) {
        if (request == null) throw new NullPointerException();

        // Security: Verify customer
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!currentUser.id().equals(request.customerId())) {
            throw new SecurityException("Cannot create order for another customer");
        }

        // Load entities
        final Person customer = this.personRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        final Store store = this.storeRepository.findById(request.storeId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Validate store is open
        if (!store.getIsOpen()) {
            throw new IllegalArgumentException("Store is currently closed");
        }

        // Validate order type
        if (request.orderType() == OrderType.DELIVERY && !store.getAcceptsDelivery()) {
            throw new IllegalArgumentException("Store does not accept delivery orders");
        }
        if (request.orderType() == OrderType.PICKUP && !store.getAcceptsPickup()) {
            throw new IllegalArgumentException("Store does not accept pickup orders");
        }

        // Load delivery address if needed
        DeliveryAddress deliveryAddress = null;
        if (request.orderType() == OrderType.DELIVERY) {
            if (request.deliveryAddressId() == null) {
                throw new IllegalArgumentException("Delivery address required for delivery orders");
            }
            deliveryAddress = this.deliveryAddressRepository.findById(request.deliveryAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Delivery address not found"));

            // Verify address belongs to customer
            if (!deliveryAddress.getCustomer().getId().equals(customer.getId())) {
                throw new SecurityException("Cannot use another customer's address");
            }
        }

        // Create Order
        Order order = new Order();
        order.setCustomer(customer);
        order.setStore(store);
        order.setOrderType(request.orderType());
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerNotes(request.customerNotes());

        // Process order items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            final MenuItem menuItem = this.menuItemRepository.findById(itemRequest.menuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemRequest.menuItemId()));

            // Verify menu item belongs to the store
            if (!menuItem.getStore().getId().equals(store.getId())) {
                throw new IllegalArgumentException("Menu item does not belong to this store");
            }

            if (!menuItem.getAvailable()) {
                throw new IllegalArgumentException("Menu item not available: " + menuItem.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setPriceAtOrder(menuItem.getPrice());
            orderItem.setSpecialInstructions(itemRequest.specialInstructions());

            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getSubtotal());
        }

        // Validate minimum order amount
        if (subtotal.compareTo(store.getMinimumOrderAmount()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Order does not meet minimum amount of €%.2f", store.getMinimumOrderAmount())
            );
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);

        // Calculate delivery fee
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (request.orderType() == OrderType.DELIVERY) {
            deliveryFee = store.getDeliveryFee();
        }
        order.setDeliveryFee(deliveryFee);

        // Calculate total
        order.setTotal(subtotal.add(deliveryFee));
        order.setCreatedAt(Instant.now());

        // Save order
        order = this.orderRepository.save(order);

        // Send notification to store owner
        try {
            final String ownerPhone = store.getOwner().getMobilePhoneNumber();
            final String content = String.format(
                    "New order #%d received! Total: €%.2f. %d items for %s.",
                    order.getId(),
                    order.getTotal(),
                    orderItems.size(),
                    request.orderType() == OrderType.DELIVERY ? "delivery" : "pickup"
            );
            boolean sent = this.smsNotificationPort.sendSms(ownerPhone, content);
            if (!sent) {
                LOGGER.warn("Failed to send SMS notification to store owner for order {}", order.getId());
            }
        } catch (Exception e) {
            LOGGER.error("Error sending notification to store owner", e);
        }

        return this.orderMapper.toView(order);
    }

    @Override
    public List<OrderView> getCustomerOrders(final Long customerId) {
        if (customerId == null || customerId <= 0) throw new IllegalArgumentException();

        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!currentUser.id().equals(customerId)) {
            throw new SecurityException("Cannot access other customer's orders");
        }

        return this.orderRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this.orderMapper::toView)
                .toList();
    }

    @Transactional
    @Override
    public void cancelOrder(final Long orderId) {
        if (orderId == null || orderId <= 0) throw new IllegalArgumentException();

        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Security: Only customer can cancel their order
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!order.getCustomer().getId().equals(currentUser.id())) {
            throw new SecurityException("Cannot cancel other customer's order");
        }

        // Can only cancel if PENDING or PREPARING
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalArgumentException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        this.orderRepository.save(order);

        // Notify store owner
        try {
            final String ownerPhone = order.getStore().getOwner().getMobilePhoneNumber();
            final String content = String.format("Order #%d has been cancelled by customer.", order.getId());
            this.smsNotificationPort.sendSms(ownerPhone, content);
        } catch (Exception e) {
            LOGGER.error("Error notifying store owner of cancellation", e);
        }
    }

    @Override
    public List<OrderView> getStoreOrders(final Long storeId) {
        if (storeId == null || storeId <= 0) throw new IllegalArgumentException();

        // Security: Only store owner or admin can view store orders
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final Store store = this.storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        if (currentUser.type() == PersonType.OWNER) {
            if (!store.getOwner().getId().equals(currentUser.id())) {
                throw new SecurityException("Cannot access other store's orders");
            }
        }

        return this.orderRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(this.orderMapper::toView)
                .toList();
    }

    @Override
    public List<OrderView> getStoreOrdersByStatus(final Long storeId, final OrderStatus status) {
        if (storeId == null || storeId <= 0) throw new IllegalArgumentException();
        if (status == null) return getStoreOrders(storeId);

        // Same security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final Store store = this.storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        if (currentUser.type() == PersonType.OWNER) {
            if (!store.getOwner().getId().equals(currentUser.id())) {
                throw new SecurityException("Cannot access other store's orders");
            }
        }

        return this.orderRepository.findAllByStoreIdAndStatus(storeId, status)
                .stream()
                .map(this.orderMapper::toView)
                .toList();
    }

    @Transactional
    @Override
    public OrderView acceptOrder(final AcceptOrderRequest request) {
        if (request == null) throw new NullPointerException();

        Order order = this.orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Security: Only store owner can accept
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!order.getStore().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can accept orders");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Can only accept PENDING orders. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PREPARING);
        order.setAcceptedAt(Instant.now());
        order = this.orderRepository.save(order);

        // Notify customer
        try {
            final String customerPhone = order.getCustomer().getMobilePhoneNumber();
            final String content = String.format(
                    "Great news! Your order #%d at %s has been accepted and is being prepared. Estimated time: %d minutes.",
                    order.getId(),
                    order.getStore().getName(),
                    order.getStore().getEstimatedDeliveryTimeMinutes()
            );
            this.smsNotificationPort.sendSms(customerPhone, content);
        } catch (Exception e) {
            LOGGER.error("Error notifying customer of order acceptance", e);
        }

        return this.orderMapper.toView(order);
    }

    @Transactional
    @Override
    public OrderView rejectOrder(final RejectOrderRequest request) {
        if (request == null) throw new NullPointerException();

        Order order = this.orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!order.getStore().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can reject orders");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Can only reject PENDING orders. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REJECTED);
        order.setRejectionReason(request.reason());
        order.setRejectedAt(Instant.now());
        order = this.orderRepository.save(order);

        // Notify customer
        try {
            final String customerPhone = order.getCustomer().getMobilePhoneNumber();
            final String content = String.format(
                    "Sorry! Your order #%d at %s was rejected. Reason: %s",
                    order.getId(),
                    order.getStore().getName(),
                    request.reason()
            );
            this.smsNotificationPort.sendSms(customerPhone, content);
        } catch (Exception e) {
            LOGGER.error("Error notifying customer of order rejection", e);
        }

        return this.orderMapper.toView(order);
    }

    @Transactional
    @Override
    public OrderView updateOrderStatus(final UpdateOrderStatusRequest request) {
        if (request == null) throw new NullPointerException();

        Order order = this.orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!order.getStore().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can update order status");
        }

        // Validate state transitions
        validateStatusTransition(order.getStatus(), request.newStatus());

        // Update status and timestamp
        order.setStatus(request.newStatus());
        switch (request.newStatus()) {
            case READY -> order.setReadyAt(Instant.now());
            case DELIVERING -> order.setDeliveringAt(Instant.now());
            case COMPLETED -> order.setCompletedAt(Instant.now());
        }

        order = this.orderRepository.save(order);

        // Notify customer
        try {
            final String customerPhone = order.getCustomer().getMobilePhoneNumber();
            String content = switch (request.newStatus()) {
                case READY -> order.getOrderType() == OrderType.PICKUP
                        ? String.format("Your order #%d is ready for pickup at %s!", order.getId(), order.getStore().getName())
                        : String.format("Your order #%d is ready and will be delivered soon!", order.getId());
                case DELIVERING -> String.format("Your order #%d is on its way!", order.getId());
                case COMPLETED -> String.format("Your order #%d has been completed. Enjoy your meal!", order.getId());
                default -> String.format("Order #%d status updated: %s", order.getId(), request.newStatus());
            };
            this.smsNotificationPort.sendSms(customerPhone, content);
        } catch (Exception e) {
            LOGGER.error("Error notifying customer of status update", e);
        }

        return this.orderMapper.toView(order);
    }

    @Override
    public Optional<OrderView> getOrder(final Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        Optional<Order> orderOpt = this.orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        // Security: Only customer, store owner, or admin can view order
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        boolean hasAccess = false;

        if (currentUser.type() == PersonType.CUSTOMER) {
            hasAccess = order.getCustomer().getId().equals(currentUser.id());
        } else if (currentUser.type() == PersonType.OWNER) {
            hasAccess = order.getStore().getOwner().getId().equals(currentUser.id());
        } else if (currentUser.type() == PersonType.ADMIN) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new SecurityException("Access denied to this order");
        }

        return Optional.of(this.orderMapper.toView(order));
    }

    @Override
    public List<OrderView> getAllOrders() {
        // Only for admin or integration API
        return this.orderRepository.findAll()
                .stream()
                .map(this.orderMapper::toView)
                .toList();
    }

    /**
     * Validates if a status transition is allowed.
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next) return;

        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.PREPARING || next == OrderStatus.REJECTED || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY -> next == OrderStatus.DELIVERING || next == OrderStatus.COMPLETED;
            case DELIVERING -> next == OrderStatus.COMPLETED;
            case COMPLETED, REJECTED, CANCELLED -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", current, next)
            );
        }
    }
}