package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.PaymentMethodType;
import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.model.SavedPaymentMethod;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.repository.SavedPaymentMethodRepository;
import com.streetfoodgo.core.service.PaymentMethodService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final SavedPaymentMethodRepository savedPaymentMethodRepository;
    private final PersonRepository personRepository;

    public PaymentMethodServiceImpl(SavedPaymentMethodRepository savedPaymentMethodRepository, PersonRepository personRepository) {
        this.savedPaymentMethodRepository = savedPaymentMethodRepository;
        this.personRepository = personRepository;
    }

    @Override
    public List<SavedPaymentMethod> getCustomerPaymentMethods(Long customerId) {
        return savedPaymentMethodRepository.findByPersonIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional
    public SavedPaymentMethod addPaymentMethod(Long customerId, PaymentMethodType type, String cardLastFour, String cardHolderName, String expiryDate) {
        Person person = personRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        SavedPaymentMethod method = new SavedPaymentMethod();
        method.setPerson(person);
        method.setType(type);
        method.setCardLastFour(cardLastFour);
        method.setCardholderName(cardHolderName);

        // Mock parsing expiry date "MM/YY" or "MM/YYYY"
        if (expiryDate != null && expiryDate.contains("/")) {
            String[] parts = expiryDate.split("/");
            if (parts.length == 2) {
                try {
                    int month = Integer.parseInt(parts[0]);
                    int year = Integer.parseInt(parts[1]);

                    // Normalize year to 4 digits if needed
                    if (year < 100) {
                        year += 2000;
                    }

                    method.setExpiryMonth(month);
                    method.setExpiryYear(year);
                } catch (NumberFormatException e) {
                    // Ignore, keep null or defaults if set
                }
            }
        }

        // Just guess brand based on first digit of last four? No, need first digit of full card.
        // We only have last four here locally. We'll just default to "Card" or maybe user selected it?
        // For now:
        method.setCardBrand("Card");
        method.setLabel("Card •••• " + cardLastFour);

        // Logic to set default: if first one, set as default
        List<SavedPaymentMethod> existing = savedPaymentMethodRepository.findByPersonId(customerId);
        method.setIsDefault(existing.isEmpty());

        method.setIsActive(true);
        method.setCreatedAt(Instant.now());

        return savedPaymentMethodRepository.save(method);
    }

    @Override
    @Transactional
    public void deletePaymentMethod(Long paymentMethodId, Long customerId) {
        SavedPaymentMethod method = savedPaymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found"));

        if (!method.getPerson().getId().equals(customerId)) {
            throw new SecurityException("Unauthorized access to payment method");
        }

        savedPaymentMethodRepository.delete(method);
    }
}
