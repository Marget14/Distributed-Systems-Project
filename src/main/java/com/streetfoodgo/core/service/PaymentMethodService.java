package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.PaymentMethodType;
import com.streetfoodgo.core.model.SavedPaymentMethod;

import java.util.List;

public interface PaymentMethodService {
    List<SavedPaymentMethod> getCustomerPaymentMethods(Long customerId);
    SavedPaymentMethod addPaymentMethod(Long customerId, PaymentMethodType type, String cardLastFour, String cardHolderName, String expiryDate);
    void deletePaymentMethod(Long paymentMethodId, Long customerId);
}
