package com.streetfoodgo.core.port;

import java.math.BigDecimal;
import java.util.List;

/**
 * Port for sending email notifications.
 */
public interface EmailPort {

    /**
     * Send a simple text email.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body (plain text)
     * @return true if email was sent successfully
     */
    boolean sendEmail(String to, String subject, String body);

    /**
     * Send an HTML email.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param htmlBody email body (HTML)
     * @return true if email was sent successfully
     */
    boolean sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Send email verification email with token.
     *
     * @param to             recipient email address
     * @param firstName      recipient's first name
     * @param verificationUrl URL to verify email
     * @return true if email was sent successfully
     */
    boolean sendVerificationEmail(String to, String firstName, String verificationUrl);

    /**
     * Send order confirmation email to customer.
     *
     * @param to          customer email
     * @param customerName customer name
     * @param orderId      order ID
     * @param storeName    store name
     * @param total        order total
     * @param orderType    "PICKUP" or "DELIVERY"
     * @return true if email was sent successfully
     */
    boolean sendOrderConfirmationEmail(String to, String customerName, Long orderId, String storeName, BigDecimal total, String orderType);

    /**
     * Send order accepted notification to customer.
     *
     * @param to          customer email
     * @param customerName customer name
     * @param orderId      order ID
     * @param storeName    store name
     * @param estimatedMinutes estimated preparation time
     * @return true if email was sent successfully
     */
    boolean sendOrderAcceptedEmail(String to, String customerName, Long orderId, String storeName, Integer estimatedMinutes);

    /**
     * Send order rejected notification to customer.
     *
     * @param to          customer email
     * @param customerName customer name
     * @param orderId      order ID
     * @param storeName    store name
     * @param rejectionReason reason for rejection
     * @return true if email was sent successfully
     */
    boolean sendOrderRejectedEmail(String to, String customerName, Long orderId, String storeName, String rejectionReason);

    /**
     * Send order status update to customer.
     *
     * @param to          customer email
     * @param customerName customer name
     * @param orderId      order ID
     * @param storeName    store name
     * @param newStatus    new order status (e.g., "PREPARING", "READY", "DELIVERING")
     * @return true if email was sent successfully
     */
    boolean sendOrderStatusUpdateEmail(String to, String customerName, Long orderId, String storeName, String newStatus);

    /**
     * Send new order notification to store owner.
     *
     * @param to       owner email
     * @param ownerName owner name
     * @param orderId   order ID
     * @param customerName customer name
     * @param itemCount number of items in order
     * @param total    order total
     * @param orderType "PICKUP" or "DELIVERY"
     * @return true if email was sent successfully
     */
    boolean sendNewOrderNotificationEmail(String to, String ownerName, Long orderId, String customerName, int itemCount, BigDecimal total, String orderType);
}


