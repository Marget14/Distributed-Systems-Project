package com.streetfoodgo.web.api;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.model.CreateDeliveryAddressRequest;
import com.streetfoodgo.core.service.model.DeliveryAddressView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class DeliveryAddressRestController {

    private final DeliveryAddressService deliveryAddressService;
    private final PersonRepository personRepository;

    public DeliveryAddressRestController(DeliveryAddressService deliveryAddressService,
                                         PersonRepository personRepository) {
        this.deliveryAddressService = deliveryAddressService;
        this.personRepository = personRepository;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryAddressView>> getUserAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long customerId = getCustomerIdFromUserDetails(userDetails);
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<DeliveryAddressView> addresses = deliveryAddressService.getCustomerAddresses(customerId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryAddressView> getAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long customerId = getCustomerIdFromUserDetails(userDetails);
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            return deliveryAddressService.getAddress(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<DeliveryAddressView> createAddress(
            @RequestBody CreateDeliveryAddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long customerId = getCustomerIdFromUserDetails(userDetails);
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            DeliveryAddressView address = deliveryAddressService.createAddress(request);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryAddressView> updateAddress(
            @PathVariable Long id,
            @RequestBody CreateDeliveryAddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long customerId = getCustomerIdFromUserDetails(userDetails);
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            DeliveryAddressView address = deliveryAddressService.updateAddress(id, request);
            return ResponseEntity.ok(address);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long customerId = getCustomerIdFromUserDetails(userDetails);
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            deliveryAddressService.deleteAddress(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long customerId = getCustomerIdFromUserDetails(userDetails);
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            deliveryAddressService.setDefaultAddress(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Long getCustomerIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        try {
            String email = userDetails.getUsername();

            Optional<Person> personOpt = personRepository.findByEmailAddressIgnoreCase(email);

            if (personOpt.isPresent()) {
                Person person = personOpt.get();
                if (person.getType() != null && person.getType().name().equals("CUSTOMER")) {
                    return person.getId();
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}