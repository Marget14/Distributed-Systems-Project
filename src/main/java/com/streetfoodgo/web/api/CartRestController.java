package com.streetfoodgo.web.api;

import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.model.MenuItemView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;

/**
 * REST API Controller for shopping cart operations.
 */
@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final MenuItemService menuItemService;

    public CartRestController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    /**
     * Add item to cart (stored in session).
     */
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody AddToCartRequest request,
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Get or create cart from session
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }

        // Get menu item details
        MenuItemView menuItem = menuItemService.getMenuItem(request.menuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        // Add or update item in cart
        CartItem cartItem = cart.get(request.menuItemId());
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        } else {
            cartItem = new CartItem(
                    menuItem.id(),
                    menuItem.name(),
                    menuItem.price(),
                    request.quantity()
            );
        }
        cart.put(request.menuItemId(), cartItem);

        // Save cart to session
        session.setAttribute("cart", cart);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("item", cartItem);
        response.put("cartSize", cart.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum());

        return ResponseEntity.ok(response);
    }

    /**
     * Get current cart.
     */
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

    /**
     * Remove item from cart.
     */
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

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Clear cart.
     */
    @DeleteMapping("/items")
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        session.removeAttribute("cart");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // ===== INNER CLASSES =====

    /**
     * Request DTO for adding items to cart.
     */
    public record AddToCartRequest(Long menuItemId, int quantity) {}

    /**
     * Cart item model stored in session.
     */
    public static class CartItem {
        private Long id;
        private String name;
        private BigDecimal price;
        private int quantity;

        public CartItem(Long id, String name, BigDecimal price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}