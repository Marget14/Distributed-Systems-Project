package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.model.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Order management.
 * Provides endpoints for customers to create and track orders,
 * and for store owners to manage orders.
 */
@RestController
@RequestMapping(value = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "APIs for order creation, management, and tracking")
public class OrderResource {

    private final OrderService orderService;

    public OrderResource(final OrderService orderService) {
        if (orderService == null) throw new NullPointerException();
        this.orderService = orderService;
    }

    /**
     * Get order details by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order details",
               description = "Retrieve detailed information about a specific order by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderView> getOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        return this.orderService.getOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all orders for the authenticated customer.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-orders")
    @Operation(summary = "Get my orders",
               description = "Retrieve all orders placed by the current authenticated customer.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not a customer")
    })
    public List<OrderView> getMyOrders(final org.springframework.security.core.Authentication authentication) {
        final long customerId = RestSecurityUtils.requireUserId(authentication);
        return this.orderService.getCustomerOrders(customerId);
    }

    /**
     * Search customer orders with optional filters.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-orders/search")
    @Operation(summary = "Search my orders",
               description = "Search customer orders with optional filters by status, order type, and date range.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format or parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<OrderView> searchCustomerOrders(
            final org.springframework.security.core.Authentication authentication,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Filter by order type (PICKUP/DELIVERY)") @RequestParam(required = false) com.streetfoodgo.core.model.OrderType orderType,
            @Parameter(description = "Start date (ISO 8601 format)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (ISO 8601 format)") @RequestParam(required = false) String endDate) {

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

    /**
     * Get customer order statistics.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-orders/statistics")
    @Operation(summary = "Get my order statistics",
               description = "Retrieve statistics about orders placed by the current customer (total orders, total spent, etc.).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public OrderStatistics getCustomerOrderStatistics(final org.springframework.security.core.Authentication authentication) {
        final long customerId = RestSecurityUtils.requireUserId(authentication);
        return this.orderService.getCustomerOrderStatistics(customerId);
    }

    /**
     * Get all orders for a store (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get store orders",
               description = "Retrieve all orders for a specific store. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not an OWNER")
    })
    public List<OrderView> getStoreOrders(
            @Parameter(description = "Store ID") @PathVariable Long storeId,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) OrderStatus status) {

        if (status != null) {
            return this.orderService.getStoreOrdersByStatus(storeId, status);
        }
        return this.orderService.getStoreOrders(storeId);
    }

    /**
     * Search store orders with optional filters.
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/store/{storeId}/search")
    @Operation(summary = "Search store orders",
               description = "Search orders for a store with optional filters by status, order type, and date range.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<OrderView> searchStoreOrders(
            @Parameter(description = "Store ID") @PathVariable Long storeId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Filter by order type") @RequestParam(required = false) com.streetfoodgo.core.model.OrderType orderType,
            @Parameter(description = "Start date (ISO 8601)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (ISO 8601)") @RequestParam(required = false) String endDate) {

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

    /**
     * Get store order statistics.
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/store/{storeId}/statistics")
    @Operation(summary = "Get store order statistics",
               description = "Retrieve statistics about orders received by a store.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public OrderStatistics getStoreOrderStatistics(
            @Parameter(description = "Store ID") @PathVariable Long storeId) {
        return this.orderService.getStoreOrderStatistics(storeId);
    }

    /**
     * Create a new order (CUSTOMER role required).
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    @Operation(summary = "Create new order",
               description = "Create a new order. The customerId is automatically taken from the JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation (e.g., store closed, minimum order not met)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not a customer")
    })
    public ResponseEntity<OrderView> createOrder(
            final org.springframework.security.core.Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order creation data (customerId will be overridden from token)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateOrderRequest.class)))
            @RequestBody @Valid CreateOrderRequest request) {

        final long customerId = RestSecurityUtils.requireUserId(authentication);
        // Force customerId from token (ignore any client-provided value)
        request = new CreateOrderRequest(
                customerId,
                request.storeId(),
                request.deliveryAddressId(),
                request.orderType(),
                request.paymentMethod(),
                request.paymentTransactionId(),
                request.items(),
                request.customerNotes()
        );

        final OrderView created = this.orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cancel an order (CUSTOMER role required).
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order",
               description = "Cancel a pending order. Can only be cancelled if not yet in delivery/pickup.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel order in current status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        this.orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Accept an order (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept order",
               description = "Store owner accepts an order. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order accepted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot accept order in current status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderView> acceptOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        final AcceptOrderRequest request = new AcceptOrderRequest(id);
        final OrderView accepted = this.orderService.acceptOrder(request);
        return ResponseEntity.ok(accepted);
    }

    /**
     * Reject an order (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject order",
               description = "Store owner rejects an order with a reason. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot reject order in current status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderView> rejectOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rejection details",
                    required = true)
            @RequestBody @Valid RejectOrderRequest request) {

        final OrderView rejected = this.orderService.rejectOrder(request);
        return ResponseEntity.ok(rejected);
    }

    /**
     * Update order status (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status",
               description = "Update the status of an order (e.g., preparing, ready, delivering). Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderView> updateOrderStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Status update details",
                    required = true)
            @RequestBody @Valid UpdateOrderStatusRequest request) {

        final OrderView updated = this.orderService.updateOrderStatus(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get all orders (integration endpoint).
     */
    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping
    @Operation(summary = "Get all orders",
               description = "Retrieve all orders in the system. Requires INTEGRATION_READ role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all orders"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<OrderView> getAllOrders() {
        return this.orderService.getAllOrders();
    }
}