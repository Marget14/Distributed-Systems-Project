package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.EmailVerificationToken;
import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.port.EmailPort;
import com.streetfoodgo.core.repository.EmailVerificationTokenRepository;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.service.EmailVerificationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implementation of EmailVerificationService.
 */
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final EmailVerificationTokenRepository tokenRepository;
    private final PersonRepository personRepository;
    private final EmailPort emailPort;
    private final String baseUrl;
    private final int tokenExpiryHours;

    public EmailVerificationServiceImpl(
            final EmailVerificationTokenRepository tokenRepository,
            final PersonRepository personRepository,
            final EmailPort emailPort,
            @Value("${app.base-url:http://localhost:8080}") final String baseUrl,
            @Value("${app.email.verification.expiry-hours:24}") final int tokenExpiryHours) {

        if (tokenRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (emailPort == null) throw new NullPointerException();

        this.tokenRepository = tokenRepository;
        this.personRepository = personRepository;
        this.emailPort = emailPort;
        this.baseUrl = baseUrl;
        this.tokenExpiryHours = tokenExpiryHours;
    }

    @Transactional
    @Override
    public boolean sendVerificationEmail(final Person person) {
        if (person == null) throw new NullPointerException();

        try {
            // Delete any existing tokens for this person
            tokenRepository.findByPerson(person).ifPresent(tokenRepository::delete);

            // Create new token
            final EmailVerificationToken token = new EmailVerificationToken(person, tokenExpiryHours);
            tokenRepository.save(token);

            // Build verification URL
            final String verificationUrl = String.format("%s/auth/verify-email?token=%s", baseUrl, token.getToken());

            // Send email
            final boolean sent = emailPort.sendVerificationEmail(
                    person.getEmailAddress(),
                    person.getFirstName(),
                    verificationUrl
            );

            if (sent) {
                LOGGER.info("Verification email sent to {}", person.getEmailAddress());
            } else {
                LOGGER.warn("Failed to send verification email to {}", person.getEmailAddress());
            }

            return sent;

        } catch (Exception e) {
            LOGGER.error("Error sending verification email to {}: {}", person.getEmailAddress(), e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    @Override
    public boolean verifyEmail(final String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }

        final EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.getIsUsed()) {
            throw new IllegalArgumentException("This verification link has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("This verification link has expired. Please request a new one.");
        }

        // Mark token as used
        verificationToken.setIsUsed(true);
        verificationToken.setVerifiedAt(Instant.now());
        tokenRepository.save(verificationToken);

        // Mark person's email as verified (we'll add this field to Person entity)
        final Person person = verificationToken.getPerson();
        person.setEmailVerified(true);
        personRepository.save(person);

        LOGGER.info("Email verified for user: {}", person.getEmailAddress());
        return true;
    }

    @Override
    public boolean isEmailVerified(final Person person) {
        if (person == null) throw new NullPointerException();
        return person.getEmailVerified() != null && person.getEmailVerified();
    }

    @Transactional
    @Override
    public boolean resendVerificationEmail(final String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or blank");
        }

        final Person person = personRepository.findByEmailAddressIgnoreCase(emailAddress)
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email address"));

        if (isEmailVerified(person)) {
            throw new IllegalArgumentException("Email address is already verified");
        }

        return sendVerificationEmail(person);
    }
}
