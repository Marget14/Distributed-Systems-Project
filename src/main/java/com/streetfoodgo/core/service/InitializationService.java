package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.*;
import com.streetfoodgo.core.repository.*;
import com.streetfoodgo.core.service.model.CreatePersonRequest;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes StreetFoodGo application with sample data.
 */
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "app.init", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final PersonRepository personRepository;
    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;
    private final PersonBusinessLogicService personBusinessLogicService;
    private final PasswordEncoder passwordEncoder;
    private final AtomicBoolean initialized;

    public InitializationService(
            final PersonRepository personRepository,
            final StoreRepository storeRepository,
            final MenuItemRepository menuItemRepository,
            final PersonBusinessLogicService personBusinessLogicService,
            final PasswordEncoder passwordEncoder) {

        if (personRepository == null) throw new NullPointerException();
        if (storeRepository == null) throw new NullPointerException();
        if (menuItemRepository == null) throw new NullPointerException();
        if (personBusinessLogicService == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();

        this.personRepository = personRepository;
        this.storeRepository = storeRepository;
        this.menuItemRepository = menuItemRepository;
        this.personBusinessLogicService = personBusinessLogicService;
        this.passwordEncoder = passwordEncoder;
        this.initialized = new AtomicBoolean(false);
    }

    @PostConstruct
    public void populateDatabaseWithInitialData() {
        final boolean alreadyInitialized = this.initialized.getAndSet(true);
        if (alreadyInitialized) {
            LOGGER.warn("Database initialization skipped: initial data has already been populated.");
            return;
        }

        LOGGER.info("Starting database initialization with StreetFoodGo data...");

        // 1. Create Users (Customers & Owners)
        createUsers();

        // 2. Create Stores & Menu Items
        createStoresAndMenuItems();

        LOGGER.info("Database initialization completed successfully!");
    }

    private void createUsers() {
        LOGGER.info("Creating users...");

        // Customers
        final List<CreatePersonRequest> customerRequests = List.of(
                new CreatePersonRequest(
                        PersonType.CUSTOMER,
                        "Dimitris",
                        "Gkoulis",
                        "gkoulis.d@example.com",
                        "+306900000001",
                        "password123"
                ),
                new CreatePersonRequest(
                        PersonType.CUSTOMER,
                        "Mara",
                        "Nikolaidou",
                        "mara.n@example.com",
                        "+306900000002",
                        "password123"
                ),
                new CreatePersonRequest(
                        PersonType.CUSTOMER,
                        "Nikos",
                        "Dimitriou",
                        "nikos.d@example.com",
                        "+306900000003",
                        "password123"
                )
        );

        // Owners
        final List<CreatePersonRequest> ownerRequests = List.of(
                new CreatePersonRequest(
                        PersonType.OWNER,
                        "Dimitris",
                        "Margetis",
                        "dimitris.m@streetfoodgo.com",
                        "+306900000014",
                        "owner123"
                ),
                new CreatePersonRequest(
                        PersonType.OWNER,
                        "Stamatis",
                        "Christofakis",
                        "stamatis.c@streetfoodgo.com",
                        "+306900000010",
                        "owner123"
                ),
                new CreatePersonRequest(
                        PersonType.OWNER,
                        "Giorgos",
                        "Koufakis",
                        "giorgos.k@streetfoodgo.com",
                        "+306900000011",
                        "owner123"
                )
        );

        // Admin
        final List<CreatePersonRequest> adminRequests = List.of(
                new CreatePersonRequest(
                        PersonType.ADMIN,
                        "Admin",
                        "User",
                        "admin@streetfoodgo.com",
                        "+306900000099",
                        "admin123"
                )
        );

        // Create all users
        for (final var request : customerRequests) {
            this.personBusinessLogicService.createPerson(request, false);
        }
        for (final var request : ownerRequests) {
            this.personBusinessLogicService.createPerson(request, false);
        }
        for (final var request : adminRequests) {
            this.personBusinessLogicService.createPerson(request, false);
        }

        LOGGER.info("Created {} users", customerRequests.size() + ownerRequests.size() + adminRequests.size());
    }

    private void createStoresAndMenuItems() {
        LOGGER.info("Creating stores and menu items...");

        // Get owners
        final Person owner1 = this.personRepository.findByEmailAddressIgnoreCase("dimitris.m@streetfoodgo.com")
                .orElseThrow();
        final Person owner2 = this.personRepository.findByEmailAddressIgnoreCase("stamatis.c@streetfoodgo.com")
                .orElseThrow();
        final Person owner3 = this.personRepository.findByEmailAddressIgnoreCase("giorgos.k@streetfoodgo.com")
                .orElseThrow();

        // Store 1: Greek Souvlaki Place
        Store store1 = new Store();
        store1.setOwner(owner1);
        store1.setName("Souvlaki Corner");
        store1.setDescription("Authentic Greek souvlaki and gyros made fresh daily");
        store1.setCuisineType(CuisineType.GREEK);
        store1.setStoreType(StoreType.FOOD_TRUCK);
        store1.setAddress("Syntagma Square, Athens");
        store1.setLatitude(37.9755);
        store1.setLongitude(23.7348);
        store1.setArea("Syntagma");
        store1.setOpeningHours("Mon-Sun: 11:00-23:00");
        store1.setIsOpen(true);
        store1.setMinimumOrderAmount(BigDecimal.valueOf(8.00));
        store1.setAcceptsDelivery(true);
        store1.setAcceptsPickup(true);
        store1.setDeliveryFee(BigDecimal.valueOf(2.50));
        store1.setEstimatedDeliveryTimeMinutes(25);
        store1.setImageUrl("https://images.unsplash.com/photo-1599487488170-d11ec9c172f0");
        store1 = this.storeRepository.save(store1);

        // Menu for Store 1
        createMenuItem(store1, "Pork Souvlaki", "Classic pork skewer with pita, tomato, onion, and tzatziki",
                BigDecimal.valueOf(3.50), MenuCategory.MAIN_COURSE);
        createMenuItem(store1, "Chicken Gyros", "Grilled chicken gyros with fresh vegetables",
                BigDecimal.valueOf(4.00), MenuCategory.MAIN_COURSE);
        createMenuItem(store1, "Greek Salad", "Fresh tomatoes, cucumber, feta, olives, and olive oil",
                BigDecimal.valueOf(5.50), MenuCategory.SALAD);
        createMenuItem(store1, "Tzatziki", "Homemade yogurt dip with cucumber and garlic",
                BigDecimal.valueOf(2.00), MenuCategory.APPETIZER);
        createMenuItem(store1, "Fries", "Crispy golden fries",
                BigDecimal.valueOf(2.50), MenuCategory.SIDE_DISH);
        createMenuItem(store1, "Coca Cola", "330ml can",
                BigDecimal.valueOf(1.50), MenuCategory.BEVERAGE);

        // Store 2: Italian Pizza
        Store store2 = new Store();
        store2.setOwner(owner2);
        store2.setName("Bella Pizza");
        store2.setDescription("Wood-fired authentic Italian pizzas");
        store2.setCuisineType(CuisineType.ITALIAN);
        store2.setStoreType(StoreType.RESTAURANT);
        store2.setAddress("Monastiraki 45, Athens");
        store2.setLatitude(37.9769);
        store2.setLongitude(23.7264);
        store2.setArea("Monastiraki");
        store2.setOpeningHours("Tue-Sun: 12:00-00:00");
        store2.setIsOpen(true);
        store2.setMinimumOrderAmount(BigDecimal.valueOf(12.00));
        store2.setAcceptsDelivery(true);
        store2.setAcceptsPickup(true);
        store2.setDeliveryFee(BigDecimal.valueOf(3.00));
        store2.setEstimatedDeliveryTimeMinutes(35);
        store2.setImageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591");
        store2 = this.storeRepository.save(store2);

        // Menu for Store 2
        createMenuItem(store2, "Margherita Pizza", "Tomato sauce, mozzarella, fresh basil",
                BigDecimal.valueOf(9.00), MenuCategory.MAIN_COURSE);
        createMenuItem(store2, "Pepperoni Pizza", "Tomato sauce, mozzarella, pepperoni",
                BigDecimal.valueOf(11.00), MenuCategory.MAIN_COURSE);
        createMenuItem(store2, "Quattro Formaggi", "Four cheese pizza with gorgonzola",
                BigDecimal.valueOf(12.50), MenuCategory.MAIN_COURSE);
        createMenuItem(store2, "Bruschetta", "Toasted bread with tomato and basil",
                BigDecimal.valueOf(5.00), MenuCategory.APPETIZER);
        createMenuItem(store2, "Tiramisu", "Classic Italian dessert",
                BigDecimal.valueOf(6.00), MenuCategory.DESSERT);
        createMenuItem(store2, "San Pellegrino", "Sparkling water 500ml",
                BigDecimal.valueOf(2.50), MenuCategory.BEVERAGE);

        // Store 3: Asian Fusion
        Store store3 = new Store();
        store3.setOwner(owner3);
        store3.setName("Wok & Roll");
        store3.setDescription("Fresh Asian cuisine with a modern twist");
        store3.setCuisineType(CuisineType.ASIAN);
        store3.setStoreType(StoreType.RESTAURANT);
        store3.setAddress("Kolonaki Square 12, Athens");
        store3.setLatitude(37.9794);
        store3.setLongitude(23.7417);
        store3.setArea("Kolonaki");
        store3.setOpeningHours("Mon-Sat: 13:00-23:00");
        store3.setIsOpen(true);
        store3.setMinimumOrderAmount(BigDecimal.valueOf(10.00));
        store3.setAcceptsDelivery(true);
        store3.setAcceptsPickup(true);
        store3.setDeliveryFee(BigDecimal.valueOf(2.50));
        store3.setEstimatedDeliveryTimeMinutes(30);
        store3.setImageUrl("https://images.unsplash.com/photo-1617093727343-374698b1b08d");
        store3 = this.storeRepository.save(store3);

        // Menu for Store 3
        createMenuItem(store3, "Pad Thai", "Traditional Thai stir-fried noodles",
                BigDecimal.valueOf(10.50), MenuCategory.MAIN_COURSE);
        createMenuItem(store3, "Chicken Ramen", "Japanese noodle soup with tender chicken",
                BigDecimal.valueOf(11.00), MenuCategory.MAIN_COURSE);
        createMenuItem(store3, "Vegetable Spring Rolls", "Crispy rolls with fresh vegetables",
                BigDecimal.valueOf(6.00), MenuCategory.APPETIZER);
        createMenuItem(store3, "Mango Sticky Rice", "Sweet Thai dessert",
                BigDecimal.valueOf(5.50), MenuCategory.DESSERT);
        createMenuItem(store3, "Green Tea", "Hot or iced",
                BigDecimal.valueOf(2.00), MenuCategory.BEVERAGE);

        // Store 4: Burger Joint (Closed for testing)
        Store store4 = new Store();
        store4.setOwner(owner1);
        store4.setName("Burger House");
        store4.setDescription("Gourmet burgers and craft beers");
        store4.setCuisineType(CuisineType.AMERICAN);
        store4.setStoreType(StoreType.RESTAURANT);
        store4.setAddress("Psiri 23, Athens");
        store4.setLatitude(37.9760);
        store4.setLongitude(23.7256);
        store4.setArea("Psiri");
        store4.setOpeningHours("Wed-Mon: 18:00-02:00");
        store4.setIsOpen(false); // CLOSED for testing
        store4.setMinimumOrderAmount(BigDecimal.valueOf(15.00));
        store4.setAcceptsDelivery(true);
        store4.setAcceptsPickup(true);
        store4.setDeliveryFee(BigDecimal.valueOf(3.50));
        store4.setEstimatedDeliveryTimeMinutes(40);
        store4.setImageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd");
        store4 = this.storeRepository.save(store4);

        createMenuItem(store4, "Classic Burger", "Beef patty with lettuce, tomato, onion",
                BigDecimal.valueOf(8.50), MenuCategory.MAIN_COURSE);
        createMenuItem(store4, "Bacon Cheeseburger", "Double patty with bacon and cheddar",
                BigDecimal.valueOf(11.50), MenuCategory.MAIN_COURSE);

        LOGGER.info("Created 4 stores with menu items");
    }

    private void createMenuItem(Store store, String name, String description,
                                BigDecimal price, MenuCategory category) {
        MenuItem item = new MenuItem();
        item.setStore(store);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setAvailable(true);
        this.menuItemRepository.save(item);
    }
}
