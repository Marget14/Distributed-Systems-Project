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
        return "/orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, final Model model) {
        final OrderView order = this.orderService.getOrder(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        model.addAttribute("order", order);
        return "/orders/detail";
    }

    @GetMapping("/new")
    public String newOrder(Model model) {
        return "/orders/new";
    }

    @GetMapping("/{id}/tracking")
    public String trackOrder(@PathVariable Long id, Model model) {
        // dummy data
        return "/orders/tracking";
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