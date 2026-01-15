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

/**
 * Implementation of EmailPort using Spring's JavaMailSender.
 */
@Component
public class EmailPortImpl implements EmailPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailPortImpl.class);

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
                            <h2>Γεια σου %s!</h2>
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
}
