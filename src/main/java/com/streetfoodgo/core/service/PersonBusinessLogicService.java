package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.CreatePersonRequest;
import com.streetfoodgo.core.service.model.CreatePersonResult;

/**
 * Service for managing Person business logic.
 */
public interface PersonBusinessLogicService {

    CreatePersonResult createPerson(CreatePersonRequest request, boolean notify);

    default CreatePersonResult createPerson(CreatePersonRequest request) {
        return this.createPerson(request, true);
    }
}