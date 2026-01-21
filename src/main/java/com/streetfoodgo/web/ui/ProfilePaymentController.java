package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.PaymentMethodType;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.PaymentMethodService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profile/payments")
@PreAuthorize("isAuthenticated()")
public class ProfilePaymentController {

    private final PaymentMethodService paymentMethodService;
    private final CurrentUserProvider currentUserProvider;

    public ProfilePaymentController(PaymentMethodService paymentMethodService, CurrentUserProvider currentUserProvider) {
        this.paymentMethodService = paymentMethodService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/new")
    public String showAddPaymentMethodForm(Model model) {
        return "profile/payment-form";
    }

    @PostMapping
    public String addPaymentMethod(
            @RequestParam("cardNumber") String cardNumber,
            @RequestParam("cardHolderName") String cardHolderName,
            @RequestParam("expiryDate") String expiryDate,
            @RequestParam("cvv") String cvv) { // CVV not saved usually, just passed "safely" to void

        final var currentUser = currentUserProvider.requireCurrentUser();

        // Mock processing: Extract last 4 digits
        String lastFour = "0000";
        if (cardNumber != null && cardNumber.length() >= 4) {
            lastFour = cardNumber.substring(cardNumber.length() - 4);
        }

        paymentMethodService.addPaymentMethod(currentUser.id(), PaymentMethodType.CARD, lastFour, cardHolderName, expiryDate);

        return "redirect:/profile#payments";
    }

    @PostMapping("/{id}/delete")
    public String deletePaymentMethod(@org.springframework.web.bind.annotation.PathVariable Long id) {
        final var currentUser = currentUserProvider.requireCurrentUser();
        try {
            paymentMethodService.deletePaymentMethod(id, currentUser.id());
        } catch (Exception e) {
            // Log error but redirect gracefully
        }
        return "redirect:/profile#payments";
    }
}
