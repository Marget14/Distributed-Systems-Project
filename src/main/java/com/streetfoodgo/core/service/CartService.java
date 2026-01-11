package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.*;
import com.streetfoodgo.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       MenuItemRepository menuItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
    }

    /**
     * Get or create cart for authenticated user
     */
    public Cart getOrCreateCart(Person person) {
        return cartRepository.findByPersonWithItems(person)
                .orElseGet(() -> {
                    Cart cart = new Cart(person);
                    return cartRepository.save(cart);
                });
    }

    /**
     * Get or create cart for guest (by session ID)
     */
    public Cart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionIdWithItems(sessionId)
                .orElseGet(() -> {
                    Cart cart = new Cart(sessionId);
                    return cartRepository.save(cart);
                });
    }

    /**
     * Add item to cart
     */
    public Cart addItem(Cart cart, Long menuItemId, Integer quantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(menuItemId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Add new item
            CartItem newItem = new CartItem(cart, menuItem, quantity, menuItem.getPrice());
            cart.addItem(newItem);
        }

        return cartRepository.save(cart);
    }

    /**
     * Update item quantity
     */
    public Cart updateItemQuantity(Cart cart, Long menuItemId, Integer quantity) {
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not in cart"));

        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
        }

        return cartRepository.save(cart);
    }

    /**
     * Remove item from cart
     */
    public Cart removeItem(Cart cart, Long menuItemId) {
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not in cart"));

        cart.removeItem(item);
        cartItemRepository.delete(item);

        return cartRepository.save(cart);
    }

    /**
     * Clear cart
     */
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Get cart count
     */
    public int getCartCount(Cart cart) {
        return cart.getTotalItems();
    }

    /**
     * Get cart total
     */
    public BigDecimal getCartTotal(Cart cart) {
        return cart.getTotalPrice();
    }
}