package com.streetfoodgo.core.port.impl;

import com.streetfoodgo.core.port.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of EmailPort using BREVO REST API (HTTP POST).
 * This sends REAL emails and satisfies the 3rd external service requirement.
 */
@Component
public class EmailPortImpl implements EmailPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailPortImpl.class);

    private static final boolean ACTIVE = true;

    // BREVO API URL
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;
    private final String senderEmail;
    private final String senderName;
    private final String apiKey;

    public EmailPortImpl(
            final RestTemplate restTemplate,
            // ΠΡΟΣΟΧΗ: Εδώ βάλε το email που δήλωσες στο Brevo
            @Value("${app.email.from:it2023141@hua.gr}") final String senderEmail,
            @Value("${app.email.sender-name:StreetFoodGo}") final String senderName,
            // ΠΡΟΣΟΧΗ: Βάλε το κλειδί σου στο application.yml ή εδώ προσωρινά για δοκιμή
            @Value("${app.email.api-key:TO_KLEIDI_SOU_EDO}") final String apiKey) {

        this.restTemplate = restTemplate;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.apiKey = apiKey;
    }

    /**
     * Helper method to send email via Brevo API.
     */
    private boolean sendViaApi(String to, String subject, String htmlContent) {
        // 1. FILTER: Only allow emails to @hua.gr addresses
        if (to == null || !to.endsWith("@hua.gr")) {
            LOGGER.warn("⛔ Skipped email to non-HUA address: {}", to);
            return false;
        }

        if (!ACTIVE) {
            LOGGER.info("📧 [MOCK API CALL] To: {} | Subject: {}", to, subject);
            return true;
        }

        try {
            // 2. Prepare Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey); // Brevo uses 'api-key' header

            // 3. Prepare Payload (Brevo JSON Format)
            // {
            //   "sender": { "name": "...", "email": "..." },
            //   "to": [ { "email": "..." } ],
            //   "subject": "...",
            //   "htmlContent": "..."
            // }

            Map<String, Object> senderMap = new HashMap<>();
            senderMap.put("name", senderName);
            senderMap.put("email", senderEmail);

            Map<String, String> recipientMap = new HashMap<>();
            recipientMap.put("email", to);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", senderMap);
            body.put("to", Collections.singletonList(recipientMap));
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            // 4. Execute HTTP POST
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(API_URL, request, String.class);

            LOGGER.info("✅ SUCCESS: Email sent via Brevo API to {}", to);
            return true;

        } catch (Exception e) {
            LOGGER.error("❌ Failed to send email via Brevo to {}: {}", to, e.getMessage());
            // Tip: Check if API Key is correct
            return false;
        }
    }

    @Override
    public boolean sendEmail(final String to, final String subject, final String body) {
        // Wrap plain text in simple HTML
        String htmlBody = "<html><body><p>" + body.replace("\n", "<br>") + "</p></body></html>";
        return sendViaApi(to, subject, htmlBody);
    }

    @Override
    public boolean sendHtmlEmail(final String to, final String subject, final String htmlBody) {
        return sendViaApi(to, subject, htmlBody);
    }

    @Override
    public boolean sendVerificationEmail(final String to, final String firstName, final String verificationUrl) {
        final String subject = "Verify Your Email - StreetFoodGo";

        final String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #ff6b35;">Hello %s!</h2>
                    <p>Welcome to StreetFoodGo! Please verify your email address to get started.</p>
                    <div style="margin: 20px 0;">
                        <a href="%s" style="background-color: #ff6b35; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Verify Email Address</a>
                    </div>
                    <p style="font-size: 12px; color: #666;">Or copy this link: %s</p>
                </body>
                </html>
                """, firstName, verificationUrl, verificationUrl);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderConfirmationEmail(String to, String customerName, Long orderId, String storeName, BigDecimal total, String orderType) {
        final String subject = "Order Confirmation #" + orderId;
        final String htmlBody = String.format("""
                <html><body>
                    <h1 style="color: #ff6b35;">Order Confirmed!</h1>
                    <p>Hi %s, your order #%d from <strong>%s</strong> has been received.</p>
                    <p><strong>Total:</strong> €%.2f</p>
                    <p>Type: %s</p>
                </body></html>
                """, customerName, orderId, storeName, total, orderType);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderAcceptedEmail(String to, String customerName, Long orderId, String storeName, Integer estimatedMinutes) {
        final String subject = "Order Accepted! #" + orderId;
        String timeInfo = estimatedMinutes != null ? estimatedMinutes + " minutes" : "soon";

        final String htmlBody = String.format("""
                <html><body>
                    <h1 style="color: #28a745;">Order Accepted!</h1>
                    <p>Store <strong>%s</strong> is preparing your order #%d.</p>
                    <p>Estimated time: <strong>%s</strong></p>
                </body></html>
                """, storeName, orderId, timeInfo);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderRejectedEmail(String to, String customerName, Long orderId, String storeName, String rejectionReason) {
        final String subject = "Order Rejected - #" + orderId;

        final String htmlBody = String.format("""
                <html><body>
                    <h1 style="color: #dc3545;">Order Rejected</h1>
                    <p>Sorry %s, store %s could not accept order #%d.</p>
                    <p>Reason: <strong>%s</strong></p>
                </body></html>
                """, customerName, storeName, orderId, rejectionReason);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderStatusUpdateEmail(String to, String customerName, Long orderId, String storeName, String newStatus) {
        final String subject = "Order Update #" + orderId;

        final String htmlBody = String.format("""
                <html><body>
                    <h1>Status Update</h1>
                    <p>Order #%d is now: <strong>%s</strong></p>
                </body></html>
                """, orderId, newStatus);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendNewOrderNotificationEmail(String to, String ownerName, Long orderId, String customerName, int itemCount, BigDecimal total, String orderType) {
        final String subject = "New Order Received! #" + orderId;

        final String htmlBody = String.format("""
                <html><body>
                    <h1>New Order!</h1>
                    <p>Owner %s, you have a new order #%d from %s.</p>
                    <p>Items: %d | Total: €%.2f | Type: %s</p>
                </body></html>
                """, ownerName, orderId, customerName, itemCount, total, orderType);

        return sendHtmlEmail(to, subject, htmlBody);
    }
}
