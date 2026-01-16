package com.streetfoodgo.core.port;

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
}
