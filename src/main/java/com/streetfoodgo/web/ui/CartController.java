package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.web.api.cart.CartLine;
import com.streetfoodgo.web.api.cart.CartSessionUtils;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.*;

/**
 * Controller for shopping cart management.
 */
@Controller
@RequestMapping("/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);

    private final StoreService storeService;
    private final MenuItemService menuItemService;

    public CartController(
            final StoreService storeService,
            final MenuItemService menuItemService) {
        this.storeService = storeService;
        this.menuItemService = menuItemService;
    }

    /**
     * Display the shopping cart with items grouped by store.
     */
    @GetMapping
    public String viewCart(
            HttpSession session,
            final Model model) {

        try {
            final List<CartLine> cartItems = CartSessionUtils.getOrCreateCart(session);

            if (cartItems.isEmpty()) {
                model.addAttribute("cart", null);
                return "cart/cart";
            }

            Map<Long, List<CartLine>> itemsByStore = new HashMap<>();
            for (CartLine item : cartItems) {
                itemsByStore.computeIfAbsent(item.getStoreId(), k -> new ArrayList<>()).add(item);
            }

            List<Map<String, Object>> storeGroups = new ArrayList<>();
            BigDecimal totalSubtotal = BigDecimal.ZERO;
            BigDecimal totalDelivery = BigDecimal.ZERO;

            for (Map.Entry<Long, List<CartLine>> entry : itemsByStore.entrySet()) {
                Long storeId = entry.getKey();
                List<CartLine> storeItems = entry.getValue();

                StoreView store = storeService.getStore(storeId)
                        .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

                BigDecimal storeSubtotal = BigDecimal.ZERO;
                List<Map<String, Object>> storeItemsList = new ArrayList<>();

                for (CartLine cartItem : storeItems) {
                    MenuItemView menuItem = menuItemService.getMenuItem(cartItem.getMenuItemId())
                            .orElse(null);

                    if (menuItem != null) {
                        BigDecimal itemTotal = menuItem.price()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                        storeSubtotal = storeSubtotal.add(itemTotal);

                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("lineId", cartItem.getLineId());
                        itemMap.put("menuItemId", menuItem.id());
                        itemMap.put("name", menuItem.name());
                        itemMap.put("description", menuItem.description());
                        itemMap.put("unitPrice", menuItem.price());
                        itemMap.put("quantity", cartItem.getQuantity());
                        itemMap.put("selectedChoiceIds", cartItem.getSelectedChoiceIds());
                        itemMap.put("removedIngredientIds", cartItem.getRemovedIngredientIds());
                        itemMap.put("specialInstructions", cartItem.getSpecialInstructions());
                        itemMap.put("totalPrice", itemTotal);
                        itemMap.put("imageUrl", menuItem.imageUrl());

                        storeItemsList.add(itemMap);
                    }
                }

                Map<String, Object> storeGroup = new HashMap<>();
                storeGroup.put("storeId", store.id());
                storeGroup.put("storeName", store.name());
                storeGroup.put("storeArea", store.area());
                storeGroup.put("items", storeItemsList);
                storeGroup.put("subtotal", storeSubtotal);
                storeGroup.put("deliveryFee", store.deliveryFee());
                storeGroup.put("minimumOrder", store.minimumOrderAmount());

                storeGroups.add(storeGroup);

                totalSubtotal = totalSubtotal.add(storeSubtotal);
                totalDelivery = totalDelivery.add(store.deliveryFee());
            }

            Map<String, Object> cart = new HashMap<>();
            cart.put("storeGroups", storeGroups);
            cart.put("items", cartItems);
            cart.put("subtotal", totalSubtotal);
            cart.put("deliveryTotal", totalDelivery);
            cart.put("serviceFee", BigDecimal.ZERO);
            cart.put("total", totalSubtotal.add(totalDelivery));

            model.addAttribute("cart", cart);
            return "cart/cart";

        } catch (Exception e) {
            LOGGER.error("Failed to load cart", e);
            model.addAttribute("cart", null);
            model.addAttribute("error", "Failed to load cart: " + e.getMessage());
            return "cart/cart";
        }
    }
}
