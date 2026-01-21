package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.*;
import com.streetfoodgo.core.port.PaymentPort;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.PaymentMethodService;
import com.streetfoodgo.core.service.model.*;
import com.streetfoodgo.web.api.cart.CartLine;
import com.streetfoodgo.web.api.cart.CartSessionUtils;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutController.class);

    private final StoreService storeService;
    private final MenuItemService menuItemService;
    private final DeliveryAddressService deliveryAddressService;
    private final OrderService orderService;
    private final PaymentPort paymentPort;
    private final CurrentUserProvider currentUserProvider;
    private final PaymentMethodService paymentMethodService;

    public CheckoutController(
            final StoreService storeService,
            final MenuItemService menuItemService,
            final DeliveryAddressService deliveryAddressService,
            final OrderService orderService,
            final PaymentPort paymentPort,
            final CurrentUserProvider currentUserProvider,
            final PaymentMethodService paymentMethodService) {

        this.storeService = storeService;
        this.menuItemService = menuItemService;
        this.deliveryAddressService = deliveryAddressService;
        this.orderService = orderService;
        this.paymentPort = paymentPort;
        this.currentUserProvider = currentUserProvider;
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Display checkout page with order summary, delivery options, and payment methods.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String showCheckout(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        
        // Get cart items
        List<CartLine> cart = CartSessionUtils.getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Το καλάθι σας είναι άδειο!");
            return "redirect:/cart";
        }

        // Get store from first item
        Long storeId = cart.get(0).getStoreId();
        StoreView store = this.storeService.getStore(storeId)
                .orElseThrow(() -> new IllegalStateException("Store not found"));

        // Calculate totals
        BigDecimal subtotal = cart.stream()
                .map(CartLine::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal deliveryFee = store.deliveryFee() != null ? store.deliveryFee() : BigDecimal.ZERO;

        // Get user's delivery addresses
        List<DeliveryAddressView> addresses = this.deliveryAddressService.getCustomerAddresses(currentUser.id());

        // Get user's payment methods
        final var paymentMethods = this.paymentMethodService.getCustomerPaymentMethods(currentUser.id());

        // Get cart line details with menu items
        List<Map<String, Object>> cartDetails = cart.stream().map(cartLine -> {
            MenuItemView menuItem = this.menuItemService.getMenuItem(cartLine.getMenuItemId()).orElse(null);
            Map<String, Object> item = new HashMap<>();
            item.put("cartLine", cartLine);
            item.put("menuItem", menuItem);
            return item;
        }).collect(Collectors.toList());

        model.addAttribute("store", store);
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("total", subtotal.add(deliveryFee));
        model.addAttribute("addresses", addresses);
        model.addAttribute("orderTypes", OrderType.values());
        model.addAttribute("paymentMethods", paymentMethods);

        return "cart/checkout";
    }

    /**
     * Process the order with payment.
     */
    @PostMapping("/process")
    @Transactional
    public String processCheckout(
            final HttpSession session,
            @RequestParam("orderType") final String orderTypeStr,
            @RequestParam(value = "deliveryAddressId", required = false) final Long deliveryAddressId,
            @RequestParam("paymentMethod") final String paymentMethodStr,
            @RequestParam(value = "savedPaymentMethodId", required = false) final Long savedPaymentMethodId,
            @RequestParam(value = "cardNumber", required = false) final String cardNumber,
            @RequestParam(value = "cardHolderName", required = false) final String cardHolderName,
            @RequestParam(value = "expiryMonth", required = false) final Integer expiryMonth,
            @RequestParam(value = "expiryYear", required = false) final Integer expiryYear,
            @RequestParam(value = "cvv", required = false) final String cvv,
            @RequestParam(value = "customerNotes", required = false) final String customerNotes,
            @RequestParam(value = "saveCard", required = false, defaultValue = "false") final boolean saveCard,
            final RedirectAttributes redirectAttributes) {

        try {
            final var currentUser = this.currentUserProvider.requireCurrentUser();
            
            // Get cart
            List<CartLine> cart = CartSessionUtils.getCart(session);
            if (cart == null || cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty!");
                return "redirect:/cart";
            }

            // Parse enums with better error handling
            OrderType orderType;
            PaymentMethod paymentMethod;
            try {
                orderType = OrderType.valueOf(orderTypeStr);
                paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid enum value: orderType={}, paymentMethod={}", orderTypeStr, paymentMethodStr);
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid order or payment method selected.");
                return "redirect:/checkout";
            }

            // For DELIVERY, delivery address is required
            if (orderType == OrderType.DELIVERY && deliveryAddressId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Delivery address is required for delivery orders.");
                return "redirect:/checkout";
            }

            // Get store
            Long storeId = cart.get(0).getStoreId();
            StoreView store = this.storeService.getStore(storeId)
                    .orElseThrow(() -> new IllegalStateException("Store not found"));

            // Calculate total
            BigDecimal subtotal = cart.stream()
                    .map(CartLine::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal deliveryFee = orderType == OrderType.DELIVERY && store.deliveryFee() != null 
                    ? store.deliveryFee() : BigDecimal.ZERO;
            BigDecimal total = subtotal.add(deliveryFee);

            // Process payment if CARD
            String transactionId = null;
            if (paymentMethod == PaymentMethod.CARD) {
                // If using a saved card
                if (savedPaymentMethodId != null) {
                    // Fetch saved card to ensure it belongs to user
                    // In a real app, uses a token. Here we mock it.
                    SavedPaymentMethod savedCard = this.paymentMethodService.getCustomerPaymentMethods(currentUser.id())
                            .stream()
                            .filter(m -> m.getId().equals(savedPaymentMethodId))
                            .findFirst()
                            .orElse(null);

                    if (savedCard == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Invalid saved card selected.");
                        return "redirect:/checkout";
                    }

                    // Mock payment with saved card
                    transactionId = "TXN-SAVED-" + UUID.randomUUID().toString();
                    LOGGER.info("Payment successful with saved card: {}", savedCard.getLabel());

                } else {
                    // New card entry
                    // Validate card details
                    if (cardNumber == null || cardNumber.isBlank() ||
                        cardHolderName == null || cardHolderName.isBlank() ||
                        expiryMonth == null || expiryYear == null ||
                        cvv == null || cvv.isBlank()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Please fill in all card details.");
                        return "redirect:/checkout";
                    }

                    PaymentPort.PaymentResult paymentResult = this.paymentPort.processCardPayment(
                            total, cardNumber, cardHolderName, expiryMonth, expiryYear, cvv);

                    if (!paymentResult.success()) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Payment failed: " + paymentResult.message());
                        return "redirect:/checkout";
                    }
                    transactionId = paymentResult.transactionId();
                    LOGGER.info("Payment successful: {}", transactionId);

                    // Save card if requested
                    if (saveCard) {
                        try {
                            String lastFour = cardNumber.length() >= 4
                                    ? cardNumber.substring(cardNumber.length() - 4)
                                    : cardNumber;
                            String expiryDate = String.format("%02d/%d", expiryMonth, expiryYear);
                            this.paymentMethodService.addPaymentMethod(
                                    currentUser.id(),
                                    PaymentMethodType.CARD,
                                    lastFour,
                                    cardHolderName,
                                    expiryDate
                            );
                        } catch (Exception e) {
                            LOGGER.warn("Failed to save payment method for user {}", currentUser.id(), e);
                            // Don't fail the order just because saving card failed
                        }
                    }
                }
            }

            // Create order request - convert CartLine to OrderItemRequest
            List<OrderItemRequest> items = cart.stream().map(cartLine -> {
                // Convert selectedChoiceIds to OrderItemCustomizationRequest
                List<OrderItemCustomizationRequest> customizations = new ArrayList<>();
                if (cartLine.getSelectedChoiceIds() != null) {
                    for (Long choiceId : cartLine.getSelectedChoiceIds()) {
                        customizations.add(new OrderItemCustomizationRequest(choiceId));
                    }
                }
                
                List<Long> removedIngredients = cartLine.getRemovedIngredientIds() != null 
                        ? cartLine.getRemovedIngredientIds() 
                        : new ArrayList<>();
                
                return new OrderItemRequest(
                        cartLine.getMenuItemId(),
                        cartLine.getQuantity(),
                        cartLine.getSpecialInstructions(),
                        customizations,
                        removedIngredients
                );
            }).collect(Collectors.toList());

            CreateOrderRequest orderRequest = new CreateOrderRequest(
                    currentUser.id(),
                    storeId,
                    deliveryAddressId,
                    orderType,
                    paymentMethod,
                    transactionId,
                    items,
                    customerNotes
            );

            // Create the order
            OrderView order = this.orderService.createOrder(orderRequest);

            // Log order creation
            LOGGER.info("Order {} created successfully with payment method: {}, transaction: {}",
                    order.id(), paymentMethod, transactionId);

            // Clear cart
            CartSessionUtils.clearCart(session);

            redirectAttributes.addFlashAttribute("successMessage", 
                    "Order placed successfully! Order number: #" + order.id());

            return "redirect:/orders/" + order.id();

        } catch (SecurityException se) {
            LOGGER.error("Security error during checkout: {}", se.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to place an order.");
            return "redirect:/checkout";
        } catch (IllegalArgumentException iae) {
            LOGGER.error("Invalid argument during checkout: {}", iae.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", iae.getMessage());
            return "redirect:/checkout";
        } catch (IllegalStateException ise) {
            LOGGER.error("Invalid state during checkout: {}", ise.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ise.getMessage());
            return "redirect:/checkout";
        } catch (Exception e) {
            LOGGER.error("Unexpected error during checkout: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An unexpected error occurred: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}
