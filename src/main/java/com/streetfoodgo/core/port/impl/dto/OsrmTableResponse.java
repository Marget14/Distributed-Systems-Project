package com.streetfoodgo.core.port.impl.dto;

import java.util.List;

/**
 * Minimal OSRM /table response model.
 *
 * https://project-osrm.org/docs/v5.27.1/api/#table-service
 */
public record OsrmTableResponse(
        String code,
        List<List<Double>> distances,
        List<List<Double>> durations
) {
}
