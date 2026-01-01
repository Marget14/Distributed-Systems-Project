package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.PersonType;

/**
 * View/DTO for Person entity.
 */
public record PersonView(
        Long id,
        String firstName,
        String lastName,
        String mobilePhoneNumber,
        String emailAddress,
        PersonType type
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}