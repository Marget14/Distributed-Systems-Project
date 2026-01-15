package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.model.UserPreferences;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.repository.UserPreferencesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final PersonRepository personRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(PersonRepository personRepository,
                             UserPreferencesRepository preferencesRepository,
                             PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.preferencesRepository = preferencesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
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

        // Format member since date
        String memberSince = "Unknown";
        if (user.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
                    .withZone(ZoneId.systemDefault());
            memberSince = formatter.format(user.getCreatedAt());
        }

        // Owners should land on the owner dashboard (owners must not order like customers)
        if (user.getType() != null && user.getType().name().equals("OWNER")) {
            return "redirect:/owner/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("memberSince", memberSince);
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

        // Get or create preferences
        UserPreferences preferences = preferencesRepository.findByPerson(user)
                .orElseGet(() -> {
                    UserPreferences newPrefs = new UserPreferences(user);
                    return preferencesRepository.save(newPrefs);
                });

        model.addAttribute("user", user);
        model.addAttribute("preferences", preferences);
        model.addAttribute("dietaryPrefs", preferences.getDietaryPreferencesSet());
        model.addAttribute("cuisines", preferences.getPreferredCuisinesSet());
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("user") Person updatedUser,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Person user = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only allowed fields (not email, password, or type)
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setMobilePhoneNumber(updatedUser.getMobilePhoneNumber());
        user.setBirthDay(updatedUser.getBirthDay());
        user.setBirthMonth(updatedUser.getBirthMonth());
        user.setBirthYear(updatedUser.getBirthYear());
        user.setGender(updatedUser.getGender());

        personRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Person user = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Current password is incorrect"));
        }

        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "New password must be at least 6 characters"));
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        personRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully"));
    }

    @PostMapping("/preferences/update")
    @ResponseBody
    public ResponseEntity<?> updatePreferences(@RequestBody PreferencesUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Person user = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreferences preferences = preferencesRepository.findByPerson(user)
                .orElseGet(() -> new UserPreferences(user));

        // Update preferences
        if (request.getLanguage() != null) {
            preferences.setLanguage(request.getLanguage());
        }
        if (request.getCurrency() != null) {
            preferences.setCurrency(request.getCurrency());
        }
        if (request.getDietaryPreferences() != null) {
            preferences.setDietaryPreferencesSet(new HashSet<>(request.getDietaryPreferences()));
        }
        if (request.getPreferredCuisines() != null) {
            preferences.setPreferredCuisinesSet(new HashSet<>(request.getPreferredCuisines()));
        }

        preferencesRepository.save(preferences);

        return ResponseEntity.ok(Map.of("success", true, "message", "Preferences updated successfully"));
    }

    @PostMapping("/notifications/update")
    @ResponseBody
    public ResponseEntity<?> updateNotifications(@RequestBody NotificationsUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Person user = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreferences preferences = preferencesRepository.findByPerson(user)
                .orElseGet(() -> new UserPreferences(user));

        // Update email notifications
        if (request.getEmailOrderUpdates() != null) {
            preferences.setEmailOrderUpdates(request.getEmailOrderUpdates());
        }
        if (request.getEmailPromotions() != null) {
            preferences.setEmailPromotions(request.getEmailPromotions());
        }
        if (request.getEmailNewRestaurants() != null) {
            preferences.setEmailNewRestaurants(request.getEmailNewRestaurants());
        }
        if (request.getEmailNewsletter() != null) {
            preferences.setEmailNewsletter(request.getEmailNewsletter());
        }

        // Update push notifications
        if (request.getPushOrderStatus() != null) {
            preferences.setPushOrderStatus(request.getPushOrderStatus());
        }
        if (request.getPushDeliveryUpdates() != null) {
            preferences.setPushDeliveryUpdates(request.getPushDeliveryUpdates());
        }
        if (request.getPushSpecialOffers() != null) {
            preferences.setPushSpecialOffers(request.getPushSpecialOffers());
        }

        // Update SMS notifications
        if (request.getSmsOrderConfirmations() != null) {
            preferences.setSmsOrderConfirmations(request.getSmsOrderConfirmations());
        }
        if (request.getSmsDeliveryUpdates() != null) {
            preferences.setSmsDeliveryUpdates(request.getSmsDeliveryUpdates());
        }

        preferencesRepository.save(preferences);

        return ResponseEntity.ok(Map.of("success", true, "message", "Notification settings updated successfully"));
    }

    // Stats inner class
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

    // DTO for password change
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    // DTO for preferences update
    public static class PreferencesUpdateRequest {
        private String language;
        private String currency;
        private List<String> dietaryPreferences;
        private List<String> preferredCuisines;

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public List<String> getDietaryPreferences() { return dietaryPreferences; }
        public void setDietaryPreferences(List<String> dietaryPreferences) { this.dietaryPreferences = dietaryPreferences; }

        public List<String> getPreferredCuisines() { return preferredCuisines; }
        public void setPreferredCuisines(List<String> preferredCuisines) { this.preferredCuisines = preferredCuisines; }
    }

    // DTO for notifications update
    public static class NotificationsUpdateRequest {
        private Boolean emailOrderUpdates;
        private Boolean emailPromotions;
        private Boolean emailNewRestaurants;
        private Boolean emailNewsletter;
        private Boolean pushOrderStatus;
        private Boolean pushDeliveryUpdates;
        private Boolean pushSpecialOffers;
        private Boolean smsOrderConfirmations;
        private Boolean smsDeliveryUpdates;

        public Boolean getEmailOrderUpdates() { return emailOrderUpdates; }
        public void setEmailOrderUpdates(Boolean emailOrderUpdates) { this.emailOrderUpdates = emailOrderUpdates; }

        public Boolean getEmailPromotions() { return emailPromotions; }
        public void setEmailPromotions(Boolean emailPromotions) { this.emailPromotions = emailPromotions; }

        public Boolean getEmailNewRestaurants() { return emailNewRestaurants; }
        public void setEmailNewRestaurants(Boolean emailNewRestaurants) { this.emailNewRestaurants = emailNewRestaurants; }

        public Boolean getEmailNewsletter() { return emailNewsletter; }
        public void setEmailNewsletter(Boolean emailNewsletter) { this.emailNewsletter = emailNewsletter; }

        public Boolean getPushOrderStatus() { return pushOrderStatus; }
        public void setPushOrderStatus(Boolean pushOrderStatus) { this.pushOrderStatus = pushOrderStatus; }

        public Boolean getPushDeliveryUpdates() { return pushDeliveryUpdates; }
        public void setPushDeliveryUpdates(Boolean pushDeliveryUpdates) { this.pushDeliveryUpdates = pushDeliveryUpdates; }

        public Boolean getPushSpecialOffers() { return pushSpecialOffers; }
        public void setPushSpecialOffers(Boolean pushSpecialOffers) { this.pushSpecialOffers = pushSpecialOffers; }

        public Boolean getSmsOrderConfirmations() { return smsOrderConfirmations; }
        public void setSmsOrderConfirmations(Boolean smsOrderConfirmations) { this.smsOrderConfirmations = smsOrderConfirmations; }

        public Boolean getSmsDeliveryUpdates() { return smsDeliveryUpdates; }
        public void setSmsDeliveryUpdates(Boolean smsDeliveryUpdates) { this.smsDeliveryUpdates = smsDeliveryUpdates; }
    }
}
