package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.DeliveryAddress;
import com.streetfoodgo.core.service.model.DeliveryAddressView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert DeliveryAddress entity to DeliveryAddressView DTO.
 */
@Component
public class DeliveryAddressMapper {

    public DeliveryAddressView toView(final DeliveryAddress address) {
        if (address == null) return null;

        return new DeliveryAddressView(
                address.getId(),
                address.getCustomer().getId(),
                address.getLabel(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getPhoneNumber(),
                address.getLatitude(),
                address.getLongitude(),
                address.getIsDefault(),
                address.getCreatedAt()
        );
    }
}