package com.streetfoodgo.core.service.model;

import java.math.BigDecimal;

public record UpdateMenuItemChoiceRequest(
        String name,
        String description,
        BigDecimal additionalPrice,
        Boolean isAvailable,
        Boolean isDefault,
        Integer displayOrder
) {}
