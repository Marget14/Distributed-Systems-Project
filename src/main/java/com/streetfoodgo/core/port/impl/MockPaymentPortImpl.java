package com.streetfoodgo.core.port.impl;

import com.streetfoodgo.core.port.PaymentPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock implementation of payment service.
 * Simulates card payment processing for demo purposes.
 */
@Component
public class MockPaymentPortImpl implements PaymentPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPaymentPortImpl.class);

    @Override
    public PaymentResult processCardPayment(
            final BigDecimal amount,
            final String cardNumber,
            final String cardHolderName,
            final int expiryMonth,
            final int expiryYear,
            final String cvv) {

        LOGGER.info("Processing mock card payment: €{} for cardholder: {}", amount, cardHolderName);

        // Validate basic inputs
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult(false, null, "Invalid amount");
        }

        if (cardNumber == null || cardNumber.length() < 13) {
            return new PaymentResult(false, null, "Invalid card number");
        }

        if (cardHolderName == null || cardHolderName.isBlank()) {
            return new PaymentResult(false, null, "Card holder name required");
        }

        if (expiryMonth < 1 || expiryMonth > 12) {
            return new PaymentResult(false, null, "Invalid expiry month");
        }

        if (expiryYear < 2024) {
            return new PaymentResult(false, null, "Card expired");
        }

        if (cvv == null || cvv.length() != 3) {
            return new PaymentResult(false, null, "Invalid CVV");
        }

        // Simulate card number checks (last digit determines success/failure)
        String lastDigit = cardNumber.substring(cardNumber.length() - 1);
        if ("0".equals(lastDigit)) {
            LOGGER.warn("Mock payment declined for card ending in 0");
            return new PaymentResult(false, null, "Payment declined by bank");
        }

        // Generate mock transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LOGGER.info("Mock payment successful. Transaction ID: {}", transactionId);
        return new PaymentResult(true, transactionId, "Payment successful");
    }
}
