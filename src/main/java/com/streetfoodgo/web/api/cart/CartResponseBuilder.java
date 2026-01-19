package com.streetfoodgo.web.api.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CartResponseBuilder {

    private CartResponseBuilder() {}

    public static Map<String, Object> toResponse(final List<CartLine> lines) {
        final Map<String, Object> response = new HashMap<>();
        response.put("items", lines);
        response.put("cartSize", lines.stream().mapToInt(CartLine::getQuantity).sum());
        return response;
    }
}
