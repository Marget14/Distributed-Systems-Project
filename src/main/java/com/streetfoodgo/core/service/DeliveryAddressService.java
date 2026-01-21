package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.CreateDeliveryAddressRequest;
import com.streetfoodgo.core.service.model.DeliveryAddressView;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing DeliveryAddress business logic.
 */
public interface DeliveryAddressService {

    List<DeliveryAddressView> getCustomerAddresses(Long customerId);

    Optional<DeliveryAddressView> getDefaultAddress(Long customerId);

    Optional<DeliveryAddressView> getAddress(Long id);

    DeliveryAddressView createAddress(CreateDeliveryAddressRequest request);

    DeliveryAddressView updateAddress(Long id, CreateDeliveryAddressRequest request);

    void setDefaultAddress(Long id);

    void deleteAddress(Long id);
}