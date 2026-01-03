package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.OrderType;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for shopping cart and checkout.
 *
 * Note: In production, use session storage or Redis for cart.
 * This is a simplified implementation.
 */
@Controller
@RequestMapping("/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final StoreService storeService;
    private final MenuItemService menuItemService;
    private final DeliveryAddressService deliveryAddressService;
    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    public CartController(
            final StoreService storeService,
            final MenuItemService menuItemService,
            final DeliveryAddressService deliveryAddressService,
            final OrderService orderService,
            final CurrentUserProvider currentUserProvider) {

        if (storeService == null) throw new NullPointerException();
        if (menuItemService == null) throw new NullPointerException();
        if (deliveryAddressService == null) throw new NullPointerException();
        if (orderService == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.storeService = storeService;
        this.menuItemService = menuItemService;
        this.deliveryAddressService = deliveryAddressService;
        this.orderService = orderService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String viewCart(final Model model) {
        // TODO: Implement proper cart session storage
        model.addAttribute("cartEmpty", true);
        return "cart/view";
    }

    @GetMapping("/checkout")
    public String showCheckout(
            @RequestParam Long storeId,
            @RequestParam String items, // Format: "itemId1:qty1,itemId2:qty2"
            final Model model) {

        final var currentUser = this.currentUserProvider.requireCurrentUser();

        final StoreView store = this.storeService.getStore(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        final List<DeliveryAddressView> addresses =
                this.deliveryAddressService.getCustomerAddresses(currentUser.id());

        model.addAttribute("store", store);
        model.addAttribute("addresses", addresses);
        model.addAttribute("items", items);
        model.addAttribute("orderTypes", OrderType.values());

        return "cart/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam Long storeId,
            @RequestParam OrderType orderType,
            @RequestParam(required = false) Long deliveryAddressId,
            @RequestParam String items, // "itemId:qty,itemId:qty"
            @RequestParam(required = false) String notes,
            final Model model) {

        final var currentUser = this.currentUserProvider.requireCurrentUser();

        // Parse items
        List<OrderItemRequest> orderItems = new ArrayList<>();
        for (String item : items.split(",")) {
            String[] parts = item.split(":");
            Long menuItemId = Long.parseLong(parts[0]);
            Integer quantity = Integer.parseInt(parts[1]);
            orderItems.add(new OrderItemRequest(menuItemId, quantity, null));
        }

        // Create order request
        CreateOrderRequest request = new CreateOrderRequest(
                currentUser.id(),
                storeId,
                orderType,
                deliveryAddressId,
                orderItems,
                notes
        );

        try {
            OrderView order = this.orderService.createOrder(request);
            return "redirect:/orders/" + order.id();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart/checkout?storeId=" + storeId + "&items=" + items + "&error";
        }
    }
}