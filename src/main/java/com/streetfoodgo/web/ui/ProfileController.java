package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
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

/**
 * Controller for user profile management.
 */
@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final CurrentUserProvider currentUserProvider;

    public ProfileController(
            final PersonRepository personRepository, 
            final PersonMapper personMapper,
            final CurrentUserProvider currentUserProvider) {
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Display user profile page.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String showProfile(final Model model) {
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        final Person person = this.personRepository.findById(currentUser.id())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        model.addAttribute("person", this.personMapper.toView(person));
        return "profile/profile";
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
}
