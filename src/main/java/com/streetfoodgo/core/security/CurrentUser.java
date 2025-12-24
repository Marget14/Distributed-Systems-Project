package com.streetfoodgo.core.security;

import com.streetfoodgo.core.model.PersonType;

/**
 * @see CurrentUserProvider
 */
public record CurrentUser(long id, String emailAddress, PersonType type) {}
