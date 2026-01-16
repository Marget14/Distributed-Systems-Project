package com.streetfoodgo.web.api.cart;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

public final class CartSessionUtils {

    private CartSessionUtils() {}

    public static final String CART_SESSION_KEY = "cart";

    @SuppressWarnings("unchecked")
    public static List<CartLine> getOrCreateCart(final HttpSession session) {
        final Object obj = session.getAttribute(CART_SESSION_KEY);
        if (obj instanceof List<?>) {
            return (List<CartLine>) obj;
        }
        final List<CartLine> cart = new ArrayList<>();
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }

    @SuppressWarnings("unchecked")
    public static List<CartLine> getCart(final HttpSession session) {
        final Object obj = session.getAttribute(CART_SESSION_KEY);
        if (obj instanceof List<?>) {
            return (List<CartLine>) obj;
        }
        return new ArrayList<>();
    }

    public static void clearCart(final HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}
