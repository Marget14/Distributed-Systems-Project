package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotNull;

public record UpdateDriverLocationRequest(
        @NotNull Double latitude,
        @NotNull Double longitude
) {}
