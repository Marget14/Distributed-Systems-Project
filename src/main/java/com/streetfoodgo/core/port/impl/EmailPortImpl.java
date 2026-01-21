package com.streetfoodgo.core.port.impl;

import com.streetfoodgo.core.port.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

/**
 * Implementation of EmailPort using Spring's JavaMailSender.
 */
@Component
public class EmailPortImpl implements EmailPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailPortImpl.class);

    private static final boolean ACTIVE = false; // Mock Mode

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailPortImpl(
            final JavaMailSender mailSender,
            @Value("${app.email.from:noreply@streetfoodgo.com}") final String fromAddress) {
        
        if (mailSender == null) throw new NullPointerException("mailSender cannot be null");
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalArgumentException("fromAddress cannot be null or blank");
        }
        
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public boolean sendEmail(final String to, final String subject, final String body) {
        if (!ACTIVE) {
            LOGGER.info("📧 [MOCK EMAIL] To: {} | Subject: {} | Body: {}", to, subject, body);
            return true;
        }

        if (to == null || to.isBlank()) {
            LOGGER.error("Cannot send email: recipient address is null or blank");
            return false;
        }

        try {
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            LOGGER.info("Email sent successfully to {}", to);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(final String to, final String subject, final String htmlBody) {
        if (to == null || to.isBlank()) {
            LOGGER.error("Cannot send email: recipient address is null or blank");
            return false;
        }

        try {
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            LOGGER.info("HTML email sent successfully to {}", to);
            return true;

        } catch (MessagingException e) {
            LOGGER.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendVerificationEmail(final String to, final String firstName, final String verificationUrl) {
        final String subject = "Verify Your Email - StreetFoodGo";
        
        final String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #ff6b35; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; }
                        .button { display: inline-block; padding: 12px 30px; background-color: #ff6b35; 
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🍴 StreetFoodGo</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s!</h2>
                            <p>Welcome to StreetFoodGo! We're excited to have you join our community of food lovers.</p>
                            <p>To complete your registration and start ordering delicious food from local stores and food trucks, 
                               please verify your email address by clicking the button below:</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">Verify Email Address</a>
                            </div>
                            <p>If the button doesn't work, copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #666;">%s</p>
                            <p><strong>This link will expire in 24 hours.</strong></p>
                            <p>If you didn't create an account with StreetFoodGo, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 StreetFoodGo. All rights reserved.</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, firstName, verificationUrl, verificationUrl);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderConfirmationEmail(String to, String customerName, Long orderId, String storeName, BigDecimal total, String orderType) {
        final String subject = "Order Confirmation #" + orderId + " - StreetFoodGo";

        String deliveryInfo = orderType.equals("DELIVERY")
                ? "Your order will be delivered to your address."
                : "Your order is ready for pickup at the store.";

        final String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #ff6b35; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; }
                        .order-info { background-color: white; padding: 15px; border-left: 4px solid #ff6b35; margin: 15px 0; }
                        .button { display: inline-block; padding: 12px 30px; background-color: #ff6b35; 
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🍴 StreetFoodGo</h1>
                        </div>
                        <div class="content">
                            <h2>Order Confirmed, %s!</h2>
                            <p>Thank you for your order! We've received it and the store is preparing it.</p>
                            
                            <div class="order-info">
                                <strong>Order #%d</strong><br>
                                Store: %s<br>
                                Total: €%.2f
                            </div>
                            
                            <p><strong>Delivery Information:</strong></p>
                            <p>%s</p>
                            
                            <p>You'll receive notifications as your order progresses through each stage. You can also track your order status in your StreetFoodGo account.</p>
                            
                            <div style="text-align: center;">
                                <a href="https://streetfoodgo.com/orders/%d/tracking" class="button">Track Your Order</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2026 StreetFoodGo. All rights reserved.</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, customerName, orderId, storeName, total, deliveryInfo, orderId);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderAcceptedEmail(String to, String customerName, Long orderId, String storeName, Integer estimatedMinutes) {
        final String subject = "Order Accepted! #" + orderId + " - StreetFoodGo";

        String timeInfo = estimatedMinutes != null
                ? String.format("Estimated time: %d minutes", estimatedMinutes)
                : "We'll notify you when your order is ready!";

        final String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; }
                        .status-box { background-color: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 15px 0; }
                        .button { display: inline-block; padding: 12px 30px; background-color: #28a745; 
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✓ Order Accepted!</h1>
                        </div>
                        <div class="content">
                            <h2>Great news, %s!</h2>
                            <p>Your order #%d from %s has been accepted and is being prepared.</p>
                            
                            <div class="status-box">
                                <strong>Status: PREPARING</strong><br>
                                %s
                            </div>
                            
                            <p>We'll notify you when your order is ready for pickup or out for delivery.</p>
                            
                            <div style="text-align: center;">
                                <a href="https://streetfoodgo.com/orders/%d/tracking" class="button">View Order Details</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2026 StreetFoodGo. All rights reserved.</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, customerName, orderId, storeName, timeInfo, orderId);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderRejectedEmail(String to, String customerName, Long orderId, String storeName, String rejectionReason) {
        final String subject = "Order Rejected - #" + orderId + " - StreetFoodGo";

        final String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; }
                        .reason-box { background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 15px 0; }
                        .button { display: inline-block; padding: 12px 30px; background-color: #ff6b35; 
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Order Rejected</h1>
                        </div>
                        <div class="content">
                            <h2>Hi %s,</h2>
                            <p>Unfortunately, your order #%d from %s could not be accepted.</p>
                            
                            <div class="reason-box">
                                <strong>Reason:</strong><br>
                                %s
                            </div>
                            
                            <p>Your payment has been refunded and you can browse other stores to place a new order.</p>
                            
                            <div style="text-align: center;">
                                <a href="https://streetfoodgo.com/stores" class="button">Browse Stores</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2026 StreetFoodGo. All rights reserved.</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, customerName, orderId, storeName, rejectionReason);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendOrderStatusUpdateEmail(String to, String customerName, Long orderId, String storeName, String newStatus) {
        final String statusIcon = switch(newStatus) {
            case "PREPARING" -> "👨‍🍳";
            case "READY" -> "✓";
            case "DELIVERING" -> "🚗";
            case "COMPLETED" -> "✓✓";
            default -> "📦";
        };

        final String statusMessage = switch(newStatus) {
            case "PREPARING" -> "Your order is being prepared with care.";
            case "READY" -> "Your order is ready! Please pick it up from the store or our driver will deliver it soon.";
            case "DELIVERING" -> "Your order is on the way! You'll receive it shortly.";
            case "COMPLETED" -> "Thank you! Your order has been completed. We hope you enjoyed your meal!";
            default -> "Your order status has been updated.";
        };

        final String headerColor = switch(newStatus) {
            case "READY" -> "#28a745";
            case "DELIVERING" -> "#17a2b8";
            case "COMPLETED" -> "#28a745";
            default -> "#ff6b35";
        };

        final String subject = "Order Status Update - #" + orderId + " - StreetFoodGo";

        final String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: %s; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; }
                        .status-box { background-color: white; padding: 15px; border-left: 4px solid %s; margin: 15px 0; }
                        .button { display: inline-block; padding: 12px 30px; background-color: %s; 
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s Order Status Update</h1>
                        </div>
                        <div class="content">
                            <h2>Hi %s,</h2>
                            <p>Your order #%d from %s has been updated.</p>
                            
                            <div class="status-box">
                                <strong>New Status: %s</strong><br>
                                %s
                            </div>
                            
                            <div style="text-align: center;">
                                <a href="https://streetfoodgo.com/orders/%d/tracking" class="button">Track Order</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2026 StreetFoodGo. All rights reserved.</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, headerColor, headerColor, headerColor, statusIcon, customerName, orderId, storeName, newStatus, statusMessage, orderId);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public boolean sendNewOrderNotificationEmail(String to, String ownerName, Long orderId, String customerName, int itemCount, BigDecimal total, String orderType) {
        final String subject = "New Order! #" + orderId + " - StreetFoodGo";

        String orderTypeInfo = orderType.equals("DELIVERY")
                ? "This order requires DELIVERY"
                : "This order is for PICKUP";

        final String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #ff6b35; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; }
                        .order-info { background-color: white; padding: 15px; border-left: 4px solid #ff6b35; margin: 15px 0; }
                        .button { display: inline-block; padding: 12px 30px; background-color: #ff6b35; 
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .action-button { display: inline-block; padding: 10px 20px; margin: 5px; 
                                        border-radius: 5px; text-decoration: none; color: white; }
                        .accept { background-color: #28a745; }
                        .reject { background-color: #dc3545; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>📦 New Order Received!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s,</h2>
                            <p>You have a new order that needs your attention!</p>
                            
                            <div class="order-info">
                                <strong>Order #%d</strong><br>
                                Customer: %s<br>
                                Items: %d<br>
                                Total: €%.2f<br>
                                Type: %s
                            </div>
                            
                            <p><strong>Quick Actions:</strong></p>
                            <p>Log in to your dashboard to view order details and take action:</p>
                            
                            <div style="text-align: center;">
                                <a href="https://streetfoodgo.com/owner/stores/1/orders" class="button">View in Dashboard</a>
                            </div>
                            
                            <p>Please accept or reject this order within 5 minutes. Your customer is waiting!</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 StreetFoodGo. All rights reserved.</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, ownerName, orderId, customerName, itemCount, total, orderTypeInfo);

        return sendHtmlEmail(to, subject, htmlBody);
    }
}
