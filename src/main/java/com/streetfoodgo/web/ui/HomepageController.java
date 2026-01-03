package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.StoreView;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for homepage.
 */
@Controller
public class HomepageController {

    private final StoreService storeService;

    public HomepageController(final StoreService storeService) {
        if (storeService == null) throw new NullPointerException();
        this.storeService = storeService;
    }

    @GetMapping("/")
    public String showHomepage(final Authentication authentication, final Model model) {
        // If authenticated, redirect to profile/stores
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/stores";
        }

        // Show featured stores on homepage
        final List<StoreView> featuredStores = this.storeService.getOpenStores()
                .stream()
                .limit(6)
                .toList();

        model.addAttribute("featuredStores", featuredStores);

        return "homepage";
    }
}