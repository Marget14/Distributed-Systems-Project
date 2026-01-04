package com.streetfoodgo.web.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for authentication (login/logout).
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
            final Authentication authentication,
            final HttpServletRequest request,
            final Model model) {

        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }

        // Show friendly messages for ?error or ?logout
        if (request.getParameter("error") != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (request.getParameter("logout") != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout(final Authentication authentication) {
        if (AuthUtils.isAnonymous(authentication)) {
            return "redirect:auth/login";
        }
        return "auth/logout";
    }
}