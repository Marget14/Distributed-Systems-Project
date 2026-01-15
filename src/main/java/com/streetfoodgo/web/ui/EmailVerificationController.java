package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.service.EmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for email verification.
 */
@Controller
@RequestMapping("/auth")
public class EmailVerificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationController.class);

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(final EmailVerificationService emailVerificationService) {
        if (emailVerificationService == null) throw new NullPointerException();
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            boolean verified = emailVerificationService.verifyEmail(token);
            if (verified) {
                model.addAttribute("successMessage", "Email verified successfully! You can now login.");
                return "auth/verification-success";
            } else {
                model.addAttribute("errorMessage", "Email verification failed. Please try again.");
                return "auth/verification-error";
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Email verification failed: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/verification-error";
        }
    }

    @GetMapping("/resend-verification")
    public String showResendForm() {
        return "auth/resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email, Model model) {
        try {
            boolean sent = emailVerificationService.resendVerificationEmail(email);
            if (sent) {
                model.addAttribute("successMessage", 
                    "Verification email has been sent! Please check your inbox.");
            } else {
                model.addAttribute("errorMessage", 
                    "Failed to send verification email. Please try again later.");
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "auth/resend-verification";
    }
}
