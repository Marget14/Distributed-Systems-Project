package com.streetfoodgo.web.api;

import com.streetfoodgo.core.model.OrderType;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.model.CreateOrderRequest;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.OrderItemCustomizationRequest;
import com.streetfoodgo.core.service.model.OrderItemRequest;
import com.streetfoodgo.core.service.model.OrderView;
import com.streetfoodgo.web.api.cart.AddToCartRequest;
import com.streetfoodgo.web.api.cart.CartLine;
import com.streetfoodgo.web.api.cart.CartLineView;
import com.streetfoodgo.web.api.cart.CartSessionUtils;
import com.streetfoodgo.web.api.cart.CheckoutRequest;
import com.streetfoodgo.web.api.cart.UpdateCustomizationRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final MenuItemService menuItemService;
    private final OrderService orderService;

    public CartRestController(final MenuItemService menuItemService, final OrderService orderService) {
        this.menuItemService = Objects.requireNonNull(menuItemService);
        this.orderService = Objects.requireNonNull(orderService);
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody @jakarta.validation.Valid AddToCartRequest request,
            HttpSession session) {

        final List<CartLine> cart = CartSessionUtils.getOrCreateCart(session);

        final MenuItemView menuItem;
        try {
            menuItem = menuItemService.getMenuItem(request.menuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        // Enforce single-store cart
        if (!cart.isEmpty()) {
            final CartLine existing = cart.get(0);
            if (!Objects.equals(existing.getStoreId(), menuItem.storeId())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Cannot mix items from different stores",
                        "currentStoreId", existing.getStoreId(),
                        "newStoreId", menuItem.storeId()
                ));
            }
        }

        final CartLine incoming = new CartLine(
                UUID.randomUUID().toString(),
                menuItem.id(),
                menuItem.storeId(),
                menuItem.name(),
                menuItem.price(),
                request.quantity()
        );
        incoming.setSelectedChoiceIds(request.selectedChoiceIds() != null ? new ArrayList<>(request.selectedChoiceIds()) : new ArrayList<>());
        incoming.setRemovedIngredientIds(request.removedIngredientIds() != null ? new ArrayList<>(request.removedIngredientIds()) : new ArrayList<>());
        incoming.setSpecialInstructions(request.specialInstructions());

        // Merge with an existing line if same customization
        for (final CartLine line : cart) {
            if (line.hasSameCustomizationAs(incoming)) {
                line.setQuantity(line.getQuantity() + incoming.getQuantity());
                return ResponseEntity.ok(cartResponse(cart));
            }
        }

        cart.add(incoming);
        session.setAttribute(CartSessionUtils.CART_SESSION_KEY, cart);

        return ResponseEntity.ok(cartResponse(cart));
    }

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        final List<CartLine> cart = CartSessionUtils.getOrCreateCart(session);
        return ResponseEntity.ok(cartResponse(cart));
    }

    @PutMapping("/items/{lineId}/quantity")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable String lineId,
            @RequestBody Map<String, Integer> body,
            HttpSession session) {

        final List<CartLine> cart = CartSessionUtils.getOrCreateCart(session);
        final int change = body.getOrDefault("change", 0);

        final Iterator<CartLine> it = cart.iterator();
        while (it.hasNext()) {
            final CartLine line = it.next();
            if (Objects.equals(line.getLineId(), lineId)) {
                final int newQuantity = line.getQuantity() + change;
                if (newQuantity <= 0) {
                    it.remove();
                } else {
                    line.setQuantity(newQuantity);
                }
                return ResponseEntity.ok(cartResponse(cart));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/items/{lineId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @PathVariable String lineId,
            HttpSession session) {

        final List<CartLine> cart = CartSessionUtils.getOrCreateCart(session);
        cart.removeIf(l -> Objects.equals(l.getLineId(), lineId));
        return ResponseEntity.ok(cartResponse(cart));
    }

    @PutMapping("/items/{lineId}/customization")
    public ResponseEntity<Map<String, Object>> updateCustomization(
            @PathVariable String lineId,
            @RequestBody UpdateCustomizationRequest request,
            HttpSession session) {

        final List<CartLine> cart = CartSessionUtils.getOrCreateCart(session);
        for (final CartLine line : cart) {
            if (Objects.equals(line.getLineId(), lineId)) {
                line.setSelectedChoiceIds(request.selectedChoiceIds() != null ? new ArrayList<>(request.selectedChoiceIds()) : new ArrayList<>());
                line.setRemovedIngredientIds(request.removedIngredientIds() != null ? new ArrayList<>(request.removedIngredientIds()) : new ArrayList<>());
                line.setSpecialInstructions(request.specialInstructions());
                return ResponseEntity.ok(cartResponse(cart));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/items")
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        session.removeAttribute(CartSessionUtils.CART_SESSION_KEY);
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

    @PutMapping("/special-instructions")
    public ResponseEntity<Map<String, Object>> setSpecialInstructions(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        String instructions = body.get("instructions");
        session.setAttribute("specialInstructions", instructions);
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
            final List<CartLine> cart = CartSessionUtils.getOrCreateCart(session);
            if (cart.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
            }

            // group by store
            final Map<Long, List<CartLine>> itemsByStore = new HashMap<>();
            for (final CartLine line : cart) {
                itemsByStore.computeIfAbsent(line.getStoreId(), k -> new ArrayList<>()).add(line);
            }

            final Long customerId = getCurrentUserId(userDetails);

            final OrderType orderType = request.orderType() != null && request.orderType().equals("PICKUP")
                    ? OrderType.PICKUP
                    : OrderType.DELIVERY;

            final List<Long> orderIds = new ArrayList<>();

            for (final Map.Entry<Long, List<CartLine>> storeEntry : itemsByStore.entrySet()) {
                final Long storeId = storeEntry.getKey();
                final List<CartLine> storeItems = storeEntry.getValue();

                final List<OrderItemRequest> orderItems = storeItems.stream()
                        .map(line -> {
                            List<OrderItemCustomizationRequest> customizations = null;
                            if (line.getSelectedChoiceIds() != null && !line.getSelectedChoiceIds().isEmpty()) {
                                customizations = line.getSelectedChoiceIds().stream().map(OrderItemCustomizationRequest::new).toList();
                            }
                            return new OrderItemRequest(
                                    line.getMenuItemId(),
                                    line.getQuantity(),
                                    line.getSpecialInstructions(),
                                    customizations,
                                    line.getRemovedIngredientIds()
                            );
                        })
                        .toList();

                final CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                        customerId,
                        storeId,
                        request.deliveryAddressId(),
                        orderType,
                        orderItems,
                        request.specialInstructions()
                );

                final OrderView order = orderService.createOrder(createOrderRequest);
                orderIds.add(order.id());
            }

            // Clear cart after successful orders
            session.removeAttribute(CartSessionUtils.CART_SESSION_KEY);
            session.removeAttribute("selectedAddressId");
            session.removeAttribute("orderType");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderIds", orderIds,
                    "storeCount", itemsByStore.size(),
                    "message", itemsByStore.size() == 1 ? "Order placed successfully" : itemsByStore.size() + " orders placed successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof com.streetfoodgo.core.security.ApplicationUserDetails appUserDetails) {
            return appUserDetails.personId();
        }
        throw new SecurityException("User not authenticated");
    }

    private Map<String, Object> cartResponse(final List<CartLine> cart) {
        final List<CartLineView> views = cart.stream().map(CartLineView::from).toList();
        return Map.of(
                "success", true,
                "items", views,
                "cartSize", views.stream().mapToInt(CartLineView::quantity).sum()
        );
    }
}
