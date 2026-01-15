package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.Person;

/**
 * Service for managing email verification tokens and process.
 */
public interface EmailVerificationService {

    /**
     * Create and send verification email to a person.
     *
     * @param person the person to verify
     * @return true if email was sent successfully
     */
    boolean sendVerificationEmail(Person person);

    /**
     * Verify email using token.
     *
     * @param token the verification token
     * @return true if verification was successful
     * @throws IllegalArgumentException if token is invalid or expired
     */
    boolean verifyEmail(String token);

    /**
     * Check if a person's email is verified.
     *
     * @param person the person to check
     * @return true if email is verified
     */
    boolean isEmailVerified(Person person);

    /**
     * Resend verification email.
     *
     * @param emailAddress the email address to resend to
     * @return true if email was sent successfully
     */
    boolean resendVerificationEmail(String emailAddress);
}
