package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.web.api.CartRestController.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.*;

/**
 * Controller for shopping cart page.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final StoreService storeService;
    private final MenuItemService menuItemService;

    public CartController(
            final StoreService storeService,
            final MenuItemService menuItemService) {
        this.storeService = storeService;
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public String viewCart(
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails,
            final Model model) {

        try {
            // Get cart from session
            @SuppressWarnings("unchecked")
            Map<Long, CartItem> cartItems = (Map<Long, CartItem>) session.getAttribute("cart");

            if (cartItems == null || cartItems.isEmpty()) {
                model.addAttribute("cart", null);
                return "cart/cart";
            }

            // Get the first item to determine store
            CartItem firstItem = cartItems.values().iterator().next();
            Long storeId = firstItem.getStoreId(); // ΧΡΗΣΙΜΟΠΟΙΗΣΕ το storeId που έχεις ήδη στο CartItem!

            // Get store info
            StoreView store = storeService.getStore(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("Store not found"));

            // Build cart structure
            List<Map<String, Object>> cartItemsList = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;

            for (CartItem item : cartItems.values()) {
                MenuItemView menuItem = menuItemService.getMenuItem(item.getId())
                        .orElse(null);

                if (menuItem != null) {
                    Map<String, Object> cartItemMap = new HashMap<>();
                    cartItemMap.put("id", menuItem.id());
                    cartItemMap.put("name", menuItem.name());
                    cartItemMap.put("description", menuItem.description());
                    cartItemMap.put("unitPrice", menuItem.price());
                    cartItemMap.put("quantity", item.getQuantity());
                    cartItemMap.put("totalPrice", menuItem.price().multiply(BigDecimal.valueOf(item.getQuantity())));
                    cartItemMap.put("imageUrl", menuItem.imageUrl());

                    cartItemsList.add(cartItemMap);
                    subtotal = subtotal.add(menuItem.price().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }

            // Build store group
            Map<String, Object> storeGroup = new HashMap<>();
            storeGroup.put("storeName", store.name());
            storeGroup.put("storeArea", store.area());
            storeGroup.put("items", cartItemsList);
            storeGroup.put("subtotal", subtotal);
            storeGroup.put("deliveryFee", store.deliveryFee());
            storeGroup.put("minimumOrder", store.minimumOrderAmount());

            // Build cart
            Map<String, Object> cart = new HashMap<>();
            cart.put("storeGroups", Collections.singletonList(storeGroup));
            cart.put("items", cartItemsList);
            cart.put("subtotal", subtotal);
            cart.put("deliveryTotal", store.deliveryFee());
            cart.put("serviceFee", BigDecimal.ZERO);
            cart.put("total", subtotal.add(store.deliveryFee()));

            model.addAttribute("cart", cart);
            model.addAttribute("storeId", store.id());

            return "cart/cart";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("cart", null);
            model.addAttribute("error", "Failed to load cart: " + e.getMessage());
            return "cart/cart";
        }
    }
}