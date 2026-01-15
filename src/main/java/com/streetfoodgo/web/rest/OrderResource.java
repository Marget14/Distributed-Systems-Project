package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.model.*;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Order management.
 */
@RestController
@RequestMapping(value = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderResource {

    private final OrderService orderService;

    public OrderResource(final OrderService orderService) {
        if (orderService == null) throw new NullPointerException();
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderView> getOrder(@PathVariable Long id) {
        return this.orderService.getOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-orders")
    public List<OrderView> getMyOrders(final org.springframework.security.core.Authentication authentication) {
        final long customerId = RestSecurityUtils.requireUserId(authentication);
        return this.orderService.getCustomerOrders(customerId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-orders/search")
    public List<OrderView> searchCustomerOrders(
            final org.springframework.security.core.Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) com.streetfoodgo.core.model.OrderType orderType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        final long customerId = RestSecurityUtils.requireUserId(authentication);

        OrderSearchCriteria.Builder builder = OrderSearchCriteria.builder()
                .customerId(customerId)
                .status(status)
                .orderType(orderType);

        if (startDate != null) {
            builder.startDate(java.time.Instant.parse(startDate));
        }
        if (endDate != null) {
            builder.endDate(java.time.Instant.parse(endDate));
        }

        return this.orderService.searchCustomerOrders(customerId, builder.build());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-orders/statistics")
    public OrderStatistics getCustomerOrderStatistics(final org.springframework.security.core.Authentication authentication) {
        final long customerId = RestSecurityUtils.requireUserId(authentication);
        return this.orderService.getCustomerOrderStatistics(customerId);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/store/{storeId}")
    public List<OrderView> getStoreOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status) {

        if (status != null) {
            return this.orderService.getStoreOrdersByStatus(storeId, status);
        }
        return this.orderService.getStoreOrders(storeId);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/store/{storeId}/search")
    public List<OrderView> searchStoreOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) com.streetfoodgo.core.model.OrderType orderType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        OrderSearchCriteria.Builder builder = OrderSearchCriteria.builder()
                .storeId(storeId)
                .status(status)
                .orderType(orderType);

        if (startDate != null) {
            builder.startDate(java.time.Instant.parse(startDate));
        }
        if (endDate != null) {
            builder.endDate(java.time.Instant.parse(endDate));
        }

        return this.orderService.searchStoreOrders(storeId, builder.build());
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/store/{storeId}/statistics")
    public OrderStatistics getStoreOrderStatistics(@PathVariable Long storeId) {
        return this.orderService.getStoreOrderStatistics(storeId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<OrderView> createOrder(
            final org.springframework.security.core.Authentication authentication,
            @RequestBody @Valid CreateOrderRequest request) {

        final long customerId = RestSecurityUtils.requireUserId(authentication);
        // Force customerId from token (ignore any client-provided value)
        request = new CreateOrderRequest(
                customerId,
                request.storeId(),
                request.deliveryAddressId(),
                request.orderType(),
                request.items(),
                request.customerNotes()
        );

        final OrderView created = this.orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        this.orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderView> acceptOrder(@PathVariable Long id) {
        final AcceptOrderRequest request = new AcceptOrderRequest(id);
        final OrderView accepted = this.orderService.acceptOrder(request);
        return ResponseEntity.ok(accepted);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<OrderView> rejectOrder(
            @PathVariable Long id,
            @RequestBody @Valid RejectOrderRequest request) {

        final OrderView rejected = this.orderService.rejectOrder(request);
        return ResponseEntity.ok(rejected);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderView> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {

        final OrderView updated = this.orderService.updateOrderStatus(request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping
    public List<OrderView> getAllOrders() {
        return this.orderService.getAllOrders();
    }
}