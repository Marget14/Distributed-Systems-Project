package com.streetfoodgo.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomepageController {

    @GetMapping("/")
    public String home(Model model) {
        // Προσθήκη dummy data για τα featured stores
        model.addAttribute("featuredStores", getDummyStores());
        return "home/homepage";  // Αλλαγή από "homepage" σε "home/homepage"
    }

    private List<Store> getDummyStores() {
        return Arrays.asList(
                new Store(1L, "QuantumBurger", "Burgers", "Fast food with quantum tech", true, 25, 12.0, "Kolonaki", true, true, 2.5),
                new Store(2L, "Sushi.AI", "Japanese", "AI-prepared sushi", true, 30, 15.0, "Syntagma", true, false, 3.0),
                new Store(3L, "Pizza Matrix", "Italian", "Neo-pizza experience", true, 20, 10.0, "Gazi", true, true, 2.0)
        );
    }

    // Simple DTO class for the example
    public static class Store {
        private Long id;
        private String name;
        private String cuisineType;
        private String description;
        private boolean isOpen;
        private int estimatedDeliveryTimeMinutes;
        private double minimumOrderAmount;
        private String area;
        private boolean acceptsDelivery;
        private boolean acceptsPickup;
        private double deliveryFee;

        public Store(Long id, String name, String cuisineType, String description, boolean isOpen,
                     int estimatedDeliveryTimeMinutes, double minimumOrderAmount, String area,
                     boolean acceptsDelivery, boolean acceptsPickup, double deliveryFee) {
            this.id = id;
            this.name = name;
            this.cuisineType = cuisineType;
            this.description = description;
            this.isOpen = isOpen;
            this.estimatedDeliveryTimeMinutes = estimatedDeliveryTimeMinutes;
            this.minimumOrderAmount = minimumOrderAmount;
            this.area = area;
            this.acceptsDelivery = acceptsDelivery;
            this.acceptsPickup = acceptsPickup;
            this.deliveryFee = deliveryFee;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getCuisineType() { return cuisineType; }
        public String getDescription() { return description; }
        public boolean isOpen() { return isOpen; }
        public int getEstimatedDeliveryTimeMinutes() { return estimatedDeliveryTimeMinutes; }
        public double getMinimumOrderAmount() { return minimumOrderAmount; }
        public String getArea() { return area; }
        public boolean acceptsDelivery() { return acceptsDelivery; }
        public boolean acceptsPickup() { return acceptsPickup; }
        public double getDeliveryFee() { return deliveryFee; }
    }
}