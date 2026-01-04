package com.streetfoodgo.core.port;

import java.util.Optional;

import com.streetfoodgo.core.model.PersonType;

/**
 * Port to external service for managing lookups.
 */
public interface LookupPort {

    Optional<PersonType> lookup(final String huaId);
}
