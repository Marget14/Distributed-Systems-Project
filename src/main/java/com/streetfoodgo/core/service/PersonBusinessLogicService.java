package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.CreatePersonRequest;
import com.streetfoodgo.core.service.model.CreatePersonResult;

/**
 * Service for managing {@link com.streetfoodgo.core.model.Person}.
 */
public interface PersonBusinessLogicService {

    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify);

    default CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
        return this.createPerson(createPersonRequest, true);
    }
}
