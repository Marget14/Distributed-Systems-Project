package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.StoreView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for checkout process.
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final StoreService storeService;

    public CheckoutController(final StoreService storeService) {
        if (storeService == null) throw new NullPointerException();
        this.storeService = storeService;
    }

    @GetMapping
    public String checkout(
            @RequestParam(required = false) Long store,
            final Model model) {

        if (store != null) {
            final StoreView storeView = this.storeService.getStore(store)
                    .orElseThrow(() -> new IllegalArgumentException("Store not found"));
            model.addAttribute("store", storeView);
        }

        return "cart/checkout";
    }
}