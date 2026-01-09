package com.streetfoodgo.core.port.impl.dto;

import com.streetfoodgo.core.model.PersonType;

/**
 * LookupResult DTO.
 */
public record LookupResult(
    String raw,
    boolean exists,
    String huaId,
    PersonType type
) {}
