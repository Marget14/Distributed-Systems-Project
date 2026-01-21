package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.DeliveryAddress;
import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.DeliveryAddressRepository;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.mapper.DeliveryAddressMapper;
import com.streetfoodgo.core.service.model.CreateDeliveryAddressRequest;
import com.streetfoodgo.core.service.model.DeliveryAddressView;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of DeliveryAddressService.
 */
@Service
public class DeliveryAddressServiceImpl implements DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;
    private final PersonRepository personRepository;
    private final DeliveryAddressMapper deliveryAddressMapper;
    private final CurrentUserProvider currentUserProvider;

    public DeliveryAddressServiceImpl(
            final DeliveryAddressRepository deliveryAddressRepository,
            final PersonRepository personRepository,
            final DeliveryAddressMapper deliveryAddressMapper,
            final CurrentUserProvider currentUserProvider) {

        if (deliveryAddressRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (deliveryAddressMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.deliveryAddressRepository = deliveryAddressRepository;
        this.personRepository = personRepository;
        this.deliveryAddressMapper = deliveryAddressMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<DeliveryAddressView> getCustomerAddresses(final Long customerId) {
        if (customerId == null || customerId <= 0) throw new IllegalArgumentException();

        // Security: Only customer can view their own addresses
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.id() !=customerId) {
            throw new SecurityException("Cannot access other customer's addresses");
        }

        return this.deliveryAddressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customerId)
                .stream()
                .map(this.deliveryAddressMapper::toView)
                .toList();
    }

    @Override
    public Optional<DeliveryAddressView> getDefaultAddress(final Long customerId) {
        if (customerId == null || customerId <= 0) throw new IllegalArgumentException();

        return this.deliveryAddressRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                .map(this.deliveryAddressMapper::toView);
    }

    @Override
    public Optional<DeliveryAddressView> getAddress(final Long id) {
        return this.deliveryAddressRepository.findById(id).map(this.deliveryAddressMapper::toView);
    }

    @Override
    @Transactional
    public DeliveryAddressView createAddress(final CreateDeliveryAddressRequest request) {
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        Person customer = this.personRepository.findById(currentUser.id())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // If this is the first address, make it default
        boolean isDefault = false;
        if (this.deliveryAddressRepository.countByCustomerId(customer.getId()) == 0) {
            isDefault = true;
        }

        DeliveryAddress address = new DeliveryAddress();
        address.setCustomer(customer);
        address.setLabel(request.label());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setNumber(request.number()); // Ensure this setter exists
        address.setArea(request.area());     // Ensure this setter exists
        address.setPostalCode(request.postalCode());
        address.setPhoneNumber(request.phoneNumber());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
        address.setIsDefault(isDefault);

        if (isDefault) {
            // Reset other defaults
            this.deliveryAddressRepository.resetDefaultAddress(customer.getId());
        }

        address = this.deliveryAddressRepository.save(address);
        return this.deliveryAddressMapper.toView(address);
    }

    @Override
    @Transactional
    public DeliveryAddressView updateAddress(final Long id, final CreateDeliveryAddressRequest request) {
        if (id == null || id <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        DeliveryAddress address = this.deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // Security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!address.getCustomer().getId().equals(currentUser.id())) {
            throw new SecurityException("Cannot update other customer's address");
        }

        address.setLabel(request.label());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setPostalCode(request.postalCode());
        address.setPhoneNumber(request.phoneNumber());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());

        address = this.deliveryAddressRepository.save(address);

        return this.deliveryAddressMapper.toView(address);
    }

    @Transactional
    @Override
    public void setDefaultAddress(final Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        DeliveryAddress address = this.deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // Security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!address.getCustomer().getId().equals(currentUser.id())) {
            throw new SecurityException("Cannot modify other customer's address");
        }

        // Unset all other defaults for this customer
        final List<DeliveryAddress> allAddresses =
                this.deliveryAddressRepository.findAllByCustomerId(currentUser.id());

        for (DeliveryAddress addr : allAddresses) {
            addr.setIsDefault(addr.getId().equals(id));
            this.deliveryAddressRepository.save(addr);
        }
    }

    @Transactional
    @Override
    public void deleteAddress(final Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        DeliveryAddress address = this.deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // Security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!address.getCustomer().getId().equals(currentUser.id())) {
            throw new SecurityException("Cannot delete other customer's address");
        }

        this.deliveryAddressRepository.delete(address);
    }
}