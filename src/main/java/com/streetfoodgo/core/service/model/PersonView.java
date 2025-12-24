package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.PersonType;
import com.streetfoodgo.core.service.PersonBusinessLogicService;

/**
 * General view of {@link com.streetfoodgo.core.model.Person} entity.
 *
 * @see com.streetfoodgo.core.model.Person
 * @see PersonBusinessLogicService
 */
public record PersonView(
        long id,
        String foodId,
        String firstName,
        String lastName,
        String mobilePhoneNumber,
        String emailAddress,
        PersonType type
) {

    public String fullName() {
        return this.firstName + " " + this.lastName;
    }
}
