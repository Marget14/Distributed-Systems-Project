package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.model.OrderView;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller for customer order management.
 */
@Controller
@RequestMapping("/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    public OrderController(
            final OrderService orderService,
            final CurrentUserProvider currentUserProvider) {

        if (orderService == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.orderService = orderService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String listOrders(final Model model) {
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final List<OrderView> orders = this.orderService.getCustomerOrders(currentUser.id());

        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, final Model model) {
        final OrderView order = this.orderService.getOrder(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        model.addAttribute("order", order);
        return "orders/detail";
    }

    @GetMapping("/new")
    public String newOrder(Model model) {
        return "orders/new";
    }

    @GetMapping("/{id}/tracking")
    public String trackOrder(@PathVariable Long id, final Model model) {
        final OrderView order = this.orderService.getOrder(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        model.addAttribute("order", order);

        // Pre-calculate safe coordinate lists for simplified Thymeleaf access
        Double storeLat = order.store() != null ? order.store().latitude() : null;
        Double storeLon = order.store() != null ? order.store().longitude() : null;
        Double custLat = order.deliveryAddress() != null ? order.deliveryAddress().latitude() : null;
        Double custLon = order.deliveryAddress() != null ? order.deliveryAddress().longitude() : null;

        // Fallback to Athens coordinates if missing
        if (storeLat == null) storeLat = 37.9838;
        if (storeLon == null) storeLon = 23.7275;
        if (custLat == null) custLat = 37.9838;
        if (custLon == null) custLon = 23.7275;

        // Add simplified attributes for the template
        model.addAttribute("storeLat", storeLat);
        model.addAttribute("storeLon", storeLon);
        model.addAttribute("custLat", custLat);
        model.addAttribute("custLon", custLon);

        // Safe strings for template
        model.addAttribute("safeStoreName", order.store() != null ? order.store().name() : "Store");
        model.addAttribute("safeStoreAddress", order.store() != null ? order.store().address() : "");
        model.addAttribute("safeDelStreet", order.deliveryAddress() != null ? order.deliveryAddress().street() : "");
        model.addAttribute("safeDelNumber", order.deliveryAddress() != null ? order.deliveryAddress().number() : "");
        model.addAttribute("safeDelCity", order.deliveryAddress() != null ? order.deliveryAddress().city() : "");
        model.addAttribute("safeDelPostalCode", order.deliveryAddress() != null ? order.deliveryAddress().postalCode() : "");
        model.addAttribute("safeDelArea", order.deliveryAddress() != null ? order.deliveryAddress().area() : "");
        model.addAttribute("safeOrderType", order.orderType() != null ? order.orderType().name() : "DELIVERY");
        model.addAttribute("safeOrderStatus", order.status() != null ? order.status().name() : "");

        // Pass driver location safely
        Double driverLat = order.driverLatitude();
        Double driverLon = order.driverLongitude();
        model.addAttribute("driverLat", driverLat);
        model.addAttribute("driverLon", driverLon);

        return "orders/tracking";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        this.orderService.cancelOrder(id);
        return "redirect:/orders/" + id;
    }

    // ========== ΠΡΟΣΘΗΚΗ ΑΥΤΟΥ ==========
    /**
     * Order confirmation page after checkout.
     */
    @GetMapping("/{orderId}/confirm")
    public String confirmOrder(
            @PathVariable Long orderId,
            final Model model) {

        model.addAttribute("orderId", orderId);
        return "orders/confirm";
    }
}