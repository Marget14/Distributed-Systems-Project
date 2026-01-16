package com.streetfoodgo.core.service.model;

public record UpdateMenuItemOptionRequest(
        String name,
        String description,
        Boolean isRequired,
        Boolean allowMultiple,
        Integer minSelections,
        Integer maxSelections,
        Integer displayOrder
) {}
