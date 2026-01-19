package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.PersonType;
import com.streetfoodgo.core.service.PersonBusinessLogicService;
import com.streetfoodgo.core.service.model.CreatePersonRequest;
import com.streetfoodgo.core.service.model.CreatePersonResult;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for user registration.
 */
@Controller
@RequestMapping("/auth")
public class RegistrationController {

    private final PersonBusinessLogicService personBusinessLogicService;

    public RegistrationController(final PersonBusinessLogicService personBusinessLogicService) {
        if (personBusinessLogicService == null) throw new NullPointerException();
        this.personBusinessLogicService = personBusinessLogicService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(
            final Authentication authentication,
            final Model model) {

        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }

        // Initial empty form (default to CUSTOMER)
        final CreatePersonRequest request = new CreatePersonRequest(
                PersonType.CUSTOMER, "", "", "", "", ""
        );
        model.addAttribute("createPersonRequest", request);

        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegistration(
            final Authentication authentication,
            @Valid @ModelAttribute("createPersonRequest") final CreatePersonRequest request,
            final BindingResult bindingResult,
            final Model model) {

        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            final CreatePersonResult result = this.personBusinessLogicService.createPerson(request);
            if (result.created()) {
                return "redirect:/login?registered";
            }
            model.addAttribute("errorMessage", result.reason());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred during registration. Please try again.");
        }
        model.addAttribute("createPersonRequest", request);
        return "auth/register";
    }
}
