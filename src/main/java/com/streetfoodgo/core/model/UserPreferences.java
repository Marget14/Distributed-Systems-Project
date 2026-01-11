package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * User preferences and notification settings.
 */
@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    // Language & Currency
    @Column(name = "language", length = 5, nullable = false)
    private String language = "en"; // en, el, es, fr, de

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "EUR"; // EUR, USD, GBP

    // Dietary Preferences (stored as comma-separated values)
    @Column(name = "dietary_preferences", length = 255)
    private String dietaryPreferences; // vegetarian,vegan,gluten-free,dairy-free,halal

    // Preferred Cuisines (stored as comma-separated values)
    @Column(name = "preferred_cuisines", length = 500)
    private String preferredCuisines; // greek,italian,japanese,mexican,burgers,pizza,sushi,asian

    // Email Notifications
    @Column(name = "email_order_updates", nullable = false)
    private Boolean emailOrderUpdates = true;

    @Column(name = "email_promotions", nullable = false)
    private Boolean emailPromotions = false;

    @Column(name = "email_new_restaurants", nullable = false)
    private Boolean emailNewRestaurants = true;

    @Column(name = "email_newsletter", nullable = false)
    private Boolean emailNewsletter = false;

    // Push Notifications
    @Column(name = "push_order_status", nullable = false)
    private Boolean pushOrderStatus = true;

    @Column(name = "push_delivery_updates", nullable = false)
    private Boolean pushDeliveryUpdates = true;

    @Column(name = "push_special_offers", nullable = false)
    private Boolean pushSpecialOffers = false;

    // SMS Notifications
    @Column(name = "sms_order_confirmations", nullable = false)
    private Boolean smsOrderConfirmations = false;

    @Column(name = "sms_delivery_updates", nullable = false)
    private Boolean smsDeliveryUpdates = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Helper methods for dietary preferences
    public Set<String> getDietaryPreferencesSet() {
        Set<String> prefs = new HashSet<>();
        if (dietaryPreferences != null && !dietaryPreferences.isEmpty()) {
            for (String pref : dietaryPreferences.split(",")) {
                prefs.add(pref.trim());
            }
        }
        return prefs;
    }

    public void setDietaryPreferencesSet(Set<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            this.dietaryPreferences = "";
        } else {
            this.dietaryPreferences = String.join(",", preferences);
        }
    }

    // Helper methods for preferred cuisines
    public Set<String> getPreferredCuisinesSet() {
        Set<String> cuisines = new HashSet<>();
        if (preferredCuisines != null && !preferredCuisines.isEmpty()) {
            for (String cuisine : preferredCuisines.split(",")) {
                cuisines.add(cuisine.trim());
            }
        }
        return cuisines;
    }

    public void setPreferredCuisinesSet(Set<String> cuisines) {
        if (cuisines == null || cuisines.isEmpty()) {
            this.preferredCuisines = "";
        } else {
            this.preferredCuisines = String.join(",", cuisines);
        }
    }

    // Constructors
    public UserPreferences() {}

    public UserPreferences(Person person) {
        this.person = person;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String dietaryPreferences) { this.dietaryPreferences = dietaryPreferences; }

    public String getPreferredCuisines() { return preferredCuisines; }
    public void setPreferredCuisines(String preferredCuisines) { this.preferredCuisines = preferredCuisines; }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}