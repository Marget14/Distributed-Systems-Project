package com.streetfoodgo.core.port;

import java.math.BigDecimal;

/**
 * Port for payment processing.
 * This is an external service interface (hexagonal architecture).
 */
public interface PaymentPort {

    /**
     * Process a card payment.
     * @param amount the amount to charge
     * @param cardNumber the card number (for demo purposes)
     * @param cardHolderName the cardholder name
     * @param expiryMonth expiry month (1-12)
     * @param expiryYear expiry year (e.g., 2025)
     * @param cvv CVV code
     * @return transaction ID if successful
     */
    PaymentResult processCardPayment(
            BigDecimal amount,
            String cardNumber,
            String cardHolderName,
            int expiryMonth,
            int expiryYear,
            String cvv
    );

    /**
     * Result of a payment transaction.
     */
    record PaymentResult(
            boolean success,
            String transactionId,
            String message
    ) {}
}
