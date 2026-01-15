package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.OrderType;
import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.DeliveryAddressView;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.web.api.cart.CartLine;
import com.streetfoodgo.web.api.cart.CartSessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/checkout")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

    private final StoreService storeService;
    private final MenuItemService menuItemService;
    private final DeliveryAddressService deliveryAddressService;
    private final PersonRepository personRepository;

    public CheckoutController(
            final StoreService storeService,
            final MenuItemService menuItemService,
            final DeliveryAddressService deliveryAddressService,
            final PersonRepository personRepository) {
        this.storeService = storeService;
        this.menuItemService = menuItemService;
        this.deliveryAddressService = deliveryAddressService;
        this.personRepository = personRepository;
    }

    @GetMapping
    public String checkout(
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails,
            final Model model,
            RedirectAttributes redirectAttributes) {

        try {
            final List<CartLine> cartItems = CartSessionUtils.getOrCreateCart(session);

            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty");
                return "redirect:/cart";
            }

            // Get order type from session, default to DELIVERY if not set
            String orderTypeStr = (String) session.getAttribute("orderType");
            if (orderTypeStr == null) {
                orderTypeStr = "DELIVERY"; // Default value instead of redirect
                session.setAttribute("orderType", orderTypeStr); // Save it to session
            }
            OrderType orderType = orderTypeStr.equals("PICKUP") ? OrderType.PICKUP : OrderType.DELIVERY;


            Map<Long, List<CartLine>> itemsByStore = new HashMap<>();
            for (CartLine item : cartItems) {
                itemsByStore.computeIfAbsent(item.getStoreId(), k -> new ArrayList<>()).add(item);
            }

            List<Map<String, Object>> storeGroups = new ArrayList<>();
            BigDecimal totalSubtotal = BigDecimal.ZERO;
            BigDecimal totalDelivery = BigDecimal.ZERO;
            boolean minimumMet = true;
            String minimumError = null;

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

                // Check minimum
                if (storeSubtotal.compareTo(store.minimumOrderAmount()) < 0) {
                    minimumMet = false;
                    minimumError = "Order for store " + store.name() + " does not meet the minimum order amount of €" + store.minimumOrderAmount();
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

            if (!minimumMet) {
                redirectAttributes.addFlashAttribute("error", minimumError);
                return "redirect:/cart";
            }

            Map<String, Object> cart = new HashMap<>();
            cart.put("storeGroups", storeGroups);
            cart.put("items", cartItems);
            cart.put("subtotal", totalSubtotal);
            cart.put("deliveryTotal", totalDelivery);
            cart.put("serviceFee", BigDecimal.ZERO);
            cart.put("total", totalSubtotal.add(totalDelivery));

            model.addAttribute("cart", cart);
            model.addAttribute("orderType", orderTypeStr);
            model.addAttribute("specialInstructions", session.getAttribute("specialInstructions"));

            if (userDetails != null && orderType == OrderType.DELIVERY) {
                Long customerId = getCustomerIdFromUserDetails(userDetails);
                if (customerId != null) {
                    List<DeliveryAddressView> userAddresses = deliveryAddressService.getCustomerAddresses(customerId);
                    model.addAttribute("userAddresses", userAddresses);
                } else {
                    model.addAttribute("userAddresses", Collections.emptyList());
                }
            } else {
                model.addAttribute("userAddresses", Collections.emptyList());
            }

            return "cart/checkout";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to load checkout: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    private Long getCustomerIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        try {
            String email = userDetails.getUsername();

            Optional<Person> personOpt = personRepository.findByEmailAddressIgnoreCase(email);

            if (personOpt.isPresent()) {
                Person person = personOpt.get();
                if (person.getType() != null && person.getType().name().equals("CUSTOMER")) {
                    return person.getId();
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
