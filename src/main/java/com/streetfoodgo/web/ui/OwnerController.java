package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.model.OrderType;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.*;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for store owner dashboard and management.
 */
@Controller
@RequestMapping("/owner")
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {

    private final StoreService storeService;
    private final MenuItemService menuItemService;
    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    public OwnerController(
            final StoreService storeService,
            final MenuItemService menuItemService,
            final OrderService orderService,
            final CurrentUserProvider currentUserProvider) {

        if (storeService == null) throw new NullPointerException();
        if (menuItemService == null) throw new NullPointerException();
        if (orderService == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.storeService = storeService;
        this.menuItemService = menuItemService;
        this.orderService = orderService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/dashboard")
    public String dashboard(final Model model) {
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final List<StoreView> myStores = this.storeService.getOwnerStores(currentUser.id());

        model.addAttribute("stores", myStores);
        return "owner/dashboard";
    }

    @GetMapping("/stores/{storeId}/orders")
    public String viewStoreOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status,
            final Model model) {

        final StoreView store = this.storeService.getStore(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        final List<OrderView> orders = status != null
                ? this.orderService.getStoreOrdersByStatus(storeId, status)
                : this.orderService.getStoreOrders(storeId);

        model.addAttribute("store", store);
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("orderStatuses", OrderStatus.values());

        return "owner/orders/orders";
    }

    @PostMapping("/orders/{orderId}/accept")
    public String acceptOrder(@PathVariable Long orderId) {
        final AcceptOrderRequest request = new AcceptOrderRequest(orderId);
        this.orderService.acceptOrder(request);

        // Redirect back to order detail
        final OrderView order = this.orderService.getOrder(orderId).orElseThrow();
        return "redirect:/owner/stores/" + order.store().id() + "/orders";
    }

    @PostMapping("/orders/{orderId}/reject")
    public String rejectOrder(
            @PathVariable Long orderId,
            @RequestParam String reason) {

        final RejectOrderRequest request = new RejectOrderRequest(orderId, reason);
        this.orderService.rejectOrder(request);

        final OrderView order = this.orderService.getOrder(orderId).orElseThrow();
        return "redirect:/owner/stores/" + order.store().id() + "/orders";
    }

    @PostMapping("/orders/{orderId}/update-status")
    public String updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {

        final UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(orderId, newStatus);
        this.orderService.updateOrderStatus(request);

        final OrderView order = this.orderService.getOrder(orderId).orElseThrow();

        // Redirect to tracking page when starting delivery
        if (newStatus == OrderStatus.DELIVERING && order.orderType() == OrderType.DELIVERY) {
            return "redirect:/owner/orders/" + orderId + "/tracking";
        }

        return "redirect:/owner/stores/" + order.store().id() + "/orders";
    }

    // Defensive GET handler to prevent 405 error if user refreshes page after status update
    @GetMapping("/orders/{orderId}/update-status")
    public String udpateOrderStatusGet(@PathVariable Long orderId) {
        final OrderView order = this.orderService.getOrder(orderId).orElseThrow();
        return "redirect:/owner/stores/" + order.store().id() + "/orders";
    }

    @GetMapping("/orders/{orderId}/tracking")
    public String trackOrder(@PathVariable Long orderId, final Model model) {
        final OrderView order = this.orderService.getOrder(orderId)
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

        // Add simplified attributes for the template (prevents rendering errors on nulls)
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

        return "owner/orders/tracking";
    }

    @GetMapping("/stores/{storeId}/menu")
    public String viewMenu(@PathVariable Long storeId, final Model model) {
        final StoreView store = this.storeService.getStore(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        final List<MenuItemView> menuItems = this.menuItemService.getStoreMenu(storeId);

        model.addAttribute("store", store);
        model.addAttribute("menuItems", menuItems);

        return "owner/menu/menu";
    }

    @GetMapping("/stores/{storeId}/menu/new")
    public String showCreateMenuItemForm(@PathVariable Long storeId, final Model model) {
        final StoreView store = this.storeService.getStore(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        model.addAttribute("store", store);
        model.addAttribute("menuItemForm", new CreateMenuItemRequest(storeId, "", "", null, null, ""));
        model.addAttribute("categories", MenuCategory.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/owner/stores/" + storeId + "/menu");

        return "owner/menu-item-form";
    }

    @GetMapping("/menu-items/{itemId}/edit")
    public String showEditMenuItemForm(@PathVariable Long itemId, final Model model) {
        final MenuItemView item = this.menuItemService.getMenuItem(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        final StoreView store = this.storeService.getStore(item.storeId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        model.addAttribute("store", store);
        model.addAttribute("menuItemForm", new UpdateMenuItemRequest(
                item.name(),
                item.description(),
                item.price(),
                item.available(),
                item.imageUrl()
        ));
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/owner/menu-items/" + itemId + "/edit");

        return "owner/menu-item-form";
    }

    @PostMapping("/menu-items/{itemId}/edit")
    public String updateMenuItem(
            @PathVariable Long itemId,
            @Valid @ModelAttribute("menuItemForm") UpdateMenuItemRequest request,
            BindingResult bindingResult,
            final Model model) {

        final MenuItemView item = this.menuItemService.getMenuItem(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        final Long storeId = item.storeId();

        if (bindingResult.hasErrors()) {
            final StoreView store = this.storeService.getStore(storeId).orElseThrow();
            model.addAttribute("store", store);
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/owner/menu-items/" + itemId + "/edit");
            return "owner/menu-item-form";
        }

        this.menuItemService.updateMenuItem(itemId, request);
        return "redirect:/owner/stores/" + storeId + "/menu";
    }

    @PostMapping("/stores/{storeId}/menu")
    public String createMenuItem(
            @PathVariable Long storeId,
            @Valid @ModelAttribute("menuItemForm") CreateMenuItemRequest request,
            BindingResult bindingResult,
            final Model model) {

        if (bindingResult.hasErrors()) {
            final StoreView store = this.storeService.getStore(storeId).orElseThrow();
            model.addAttribute("store", store);
            return "owner/menu-item-form";
        }

        this.menuItemService.createMenuItem(request);
        return "redirect:/owner/stores/" + storeId + "/menu";
    }

    @PostMapping("/menu-items/{itemId}/toggle")
    public String toggleAvailability(
            @PathVariable Long itemId,
            @RequestParam boolean available) {

        this.menuItemService.toggleAvailability(itemId, available);

        final MenuItemView item = this.menuItemService.getMenuItem(itemId).orElseThrow();
        return "redirect:/owner/stores/" + item.storeId() + "/menu";
    }

    @PostMapping("/menu-items/{itemId}/delete")
    public String deleteMenuItem(@PathVariable Long itemId) {
        final MenuItemView item = this.menuItemService.getMenuItem(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        this.menuItemService.deleteMenuItem(itemId);
        return "redirect:/owner/stores/" + item.storeId() + "/menu";
    }

    @GetMapping("/stores/{storeId}/edit")
    public String showEditStoreForm(@PathVariable Long storeId, final Model model) {
        final StoreView store = this.storeService.getStore(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        model.addAttribute("store", store);
        model.addAttribute("storeForm", new UpdateStoreRequest(
                store.name(),
                store.description(),
                store.openingHours(),
                store.isOpen(),
                store.minimumOrderAmount(),
                store.acceptsDelivery(),
                store.acceptsPickup(),
                store.deliveryFee(),
                store.estimatedDeliveryTimeMinutes(),
                store.imageUrl()
        ));

        return "owner/store/edit";
    }

    @PostMapping("/stores/{storeId}/edit")
    public String updateStore(
            @PathVariable Long storeId,
            @Valid @ModelAttribute("storeForm") UpdateStoreRequest request,
            BindingResult bindingResult,
            final Model model) {

        if (bindingResult.hasErrors()) {
            final StoreView store = this.storeService.getStore(storeId).orElseThrow();
            model.addAttribute("store", store);
            return "owner/store/edit";
        }

        this.storeService.updateStore(storeId, request);
        return "redirect:/owner/dashboard";
    }
}

