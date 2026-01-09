package com.streetfoodgo.web.api;

import com.streetfoodgo.core.model.OrderType;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final MenuItemService menuItemService;
    private final OrderService orderService;

    public CartRestController(
            MenuItemService menuItemService,
            OrderService orderService) {
        this.menuItemService = menuItemService;
        this.orderService = orderService;
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody AddToCartRequest request,
            HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }

        MenuItemView menuItem = menuItemService.getMenuItem(request.menuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        CartItem cartItem = cart.get(request.menuItemId());
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        } else {
            cartItem = new CartItem(
                    menuItem.id(),
                    menuItem.storeId(),
                    menuItem.name(),
                    menuItem.price(),
                    request.quantity()
            );
        }
        cart.put(request.menuItemId(), cartItem);
        session.setAttribute("cart", cart);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("item", cartItem);
        response.put("cartSize", cart.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        Map<String, Object> response = new HashMap<>();
        response.put("items", cart != null ? cart.values() : Collections.emptyList());
        response.put("cartSize", cart != null ?
                cart.values().stream().mapToInt(CartItem::getQuantity).sum() : 0);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{itemId}/quantity")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body,
            HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        if (cart == null || !cart.containsKey(itemId)) {
            return ResponseEntity.notFound().build();
        }

        CartItem item = cart.get(itemId);
        int change = body.get("change");
        int newQuantity = item.getQuantity() + change;

        if (newQuantity <= 0) {
            cart.remove(itemId);
        } else {
            item.setQuantity(newQuantity);
        }

        session.setAttribute("cart", cart);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newQuantity", newQuantity);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @PathVariable Long itemId,
            HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        if (cart != null) {
            cart.remove(itemId);
            session.setAttribute("cart", cart);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/items")
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        session.removeAttribute("cart");
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/delivery-address")
    public ResponseEntity<Map<String, Object>> setDeliveryAddress(
            @RequestBody Map<String, Long> body,
            HttpSession session) {

        Long addressId = body.get("addressId");
        session.setAttribute("selectedAddressId", addressId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/order-type")
    public ResponseEntity<Map<String, Object>> setOrderType(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        String orderType = body.get("orderType");
        session.setAttribute("orderType", orderType);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * CHECKOUT - Creates actual order via OrderService
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(
            @RequestBody CheckoutRequest request,
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // Get cart from session
            @SuppressWarnings("unchecked")
            Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

            if (cart == null || cart.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cart is empty"));
            }

            // Get storeId from first item
            Long storeId = cart.values().iterator().next().getStoreId();

            // Get current user ID
            Long customerId = getCurrentUserId(userDetails);

            // Build order items
            List<OrderItemRequest> orderItems = cart.values().stream()
                    .map(item -> new OrderItemRequest(
                            item.getId(),
                            item.getQuantity(),
                            null // special instructions
                    ))
                    .toList();

            // Determine order type
            OrderType orderType = request.orderType() != null && request.orderType().equals("PICKUP")
                    ? OrderType.PICKUP
                    : OrderType.DELIVERY;

            // Create order via OrderService
            CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                    customerId,
                    storeId,
                    request.deliveryAddressId(),
                    orderType,
                    orderItems,
                    request.specialInstructions()
            );

            OrderView order = orderService.createOrder(createOrderRequest);

            // Clear cart after successful order
            session.removeAttribute("cart");
            session.removeAttribute("selectedAddressId");
            session.removeAttribute("orderType");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.id());
            response.put("message", "Order placed successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Helper to get current user ID from UserDetails
     */
    private Long getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof com.streetfoodgo.core.security.ApplicationUserDetails appUserDetails) {
            return appUserDetails.personId();
        }
        throw new SecurityException("User not authenticated");
    }

    // ===== DTOs =====

    public record AddToCartRequest(Long menuItemId, int quantity) {}

    public record CheckoutRequest(
            Long deliveryAddressId,
            String orderType,
            String specialInstructions
    ) {}

    public static class CartItem {
        private Long id;
        private Long storeId;
        private String name;
        private BigDecimal price;
        private int quantity;

        public CartItem(Long id, Long storeId, String name, BigDecimal price, int quantity) {
            this.id = id;
            this.storeId = storeId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
