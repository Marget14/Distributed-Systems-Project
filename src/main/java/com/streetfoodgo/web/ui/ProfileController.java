package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.PersonRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final PersonRepository personRepository;

    public ProfileController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Person user = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stats stats = new Stats();
        stats.setTotalOrders(0);
        stats.setCompletedOrders(0);
        stats.setTotalSpent("0.00");

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        return "profile/profile";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Person user = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "profile/edit";
    }

    public static class Stats {
        private int totalOrders;
        private int completedOrders;
        private String totalSpent;
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        public int getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
        public String getTotalSpent() { return totalSpent; }
        public void setTotalSpent(String totalSpent) { this.totalSpent = totalSpent; }
    }
}