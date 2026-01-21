package com.streetfoodgo.core.security;

import com.streetfoodgo.core.port.EmailPort;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginNotificationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final EmailPort emailPort;

    public LoginNotificationSuccessHandler(EmailPort emailPort) {
        this.emailPort = emailPort;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        String email = null;
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User user) {
             email = user.getUsername();
        }

        if (email != null && !email.startsWith("user:")) { // Avoid internal technical users if any
            final String userEmail = email;
            // Run in a separate thread to avoid blocking login response
            new Thread(() -> {
                try {
                    String subject = "Login Notification - StreetFoodGo";
                    String body = "<html><body><h1>Login Detected</h1><p>A new sign-in to your account was detected.</p></body></html>";
                    emailPort.sendEmail(userEmail, subject, body);
                } catch (Exception e) {
                    logger.error("Failed to send login notification", e);
                }
            }).start();
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
