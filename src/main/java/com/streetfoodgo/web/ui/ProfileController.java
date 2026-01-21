package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.DeliveryAddressRepository;
import com.streetfoodgo.core.repository.OrderRepository;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.PaymentMethodService;
import com.streetfoodgo.core.service.mapper.PersonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Controller for user profile management.
 */
@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    private final PersonRepository personRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final OrderRepository orderRepository;
    private final PersonMapper personMapper;
    private final CurrentUserProvider currentUserProvider;
    private final PasswordEncoder passwordEncoder;
    private final PaymentMethodService paymentMethodService;

    public ProfileController(
            final PersonRepository personRepository,
            final DeliveryAddressRepository deliveryAddressRepository,
            final OrderRepository orderRepository,
            final PersonMapper personMapper,
            final CurrentUserProvider currentUserProvider,
            final PasswordEncoder passwordEncoder,
            final PaymentMethodService paymentMethodService) {
        if (personRepository == null) throw new NullPointerException();
        if (deliveryAddressRepository == null) throw new NullPointerException();
        if (orderRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        if (paymentMethodService == null) throw new NullPointerException();
        this.personRepository = personRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.orderRepository = orderRepository;
        this.personMapper = personMapper;
        this.currentUserProvider = currentUserProvider;
        this.passwordEncoder = passwordEncoder;
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Display user profile page with all info (efood style).
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String showProfile(final Model model) {
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final Person person = this.personRepository.findById(currentUser.id())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Load delivery addresses
        final var addresses = this.deliveryAddressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(currentUser.id());

        // Load payment methods
        final var paymentMethods = this.paymentMethodService.getCustomerPaymentMethods(currentUser.id());

        // Count orders
        final long orderCount = this.orderRepository.countByCustomerId(currentUser.id());

        model.addAttribute("person", this.personMapper.toView(person));
        model.addAttribute("deliveryAddresses", addresses);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("addressCount", (long) addresses.size());
        model.addAttribute("orderCount", orderCount);

        return "profile/profile-new";
    }

    /**
     * Show edit profile form.
     */
    @GetMapping("/edit")
    @Transactional(readOnly = true)
    public String showEditForm(final Model model) {
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final Person person = this.personRepository.findById(currentUser.id())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        model.addAttribute("person", this.personMapper.toView(person));
        return "profile/edit";
    }

    /**
     * Update profile information.
     */
    @PostMapping("/edit")
    @Transactional
    public String updateProfile(
            @RequestParam("firstName") final String firstName,
            @RequestParam("lastName") final String lastName,
            @RequestParam("mobilePhoneNumber") final String mobilePhoneNumber,
            final RedirectAttributes redirectAttributes) {

        try {
            final var currentUser = this.currentUserProvider.requireCurrentUser();
            
            // Get current person
            final Person person = this.personRepository.findById(currentUser.id())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            // Update basic info
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setMobilePhoneNumber(mobilePhoneNumber);
            
            this.personRepository.save(person);
            
            LOGGER.info("Updated profile for user {}: {} {}", currentUser.id(), firstName, lastName);

            redirectAttributes.addFlashAttribute("successMessage", "Το προφίλ ενημερώθηκε επιτυχώς!");
            return "redirect:/profile";

        } catch (Exception e) {
            LOGGER.error("Failed to update profile", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Σφάλμα κατά την ενημέρωση του προφίλ.");
            return "redirect:/profile/edit";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    @Transactional
    public String changePassword(
            @RequestParam("currentPassword") final String currentPassword,
            @RequestParam("newPassword") final String newPassword,
            @RequestParam("confirmPassword") final String confirmPassword,
            final RedirectAttributes redirectAttributes) {

        try {
            final var currentUser = this.currentUserProvider.requireCurrentUser();
            final Person person = this.personRepository.findById(currentUser.id())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            // Verify current password
            if (!this.passwordEncoder.matches(currentPassword, person.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
                return "redirect:/profile/change-password";
            }

            // Verify new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
                return "redirect:/profile/change-password";
            }

            // Update password
            person.setPasswordHash(this.passwordEncoder.encode(newPassword));
            this.personRepository.save(person);

            LOGGER.info("Password changed for user {}", currentUser.id());
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:/profile";

        } catch (Exception e) {
            LOGGER.error("Failed to change password", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change password. Please try again.");
            return "redirect:/profile/change-password";
        }
    }
}
