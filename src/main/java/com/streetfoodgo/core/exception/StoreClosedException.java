package com.streetfoodgo.core.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when trying to order from a closed store.
 */
public class StoreClosedException extends BusinessException {

    private final LocalDateTime nextOpeningTime;

    public StoreClosedException(String message) {
        super(message, "STORE_CLOSED");
        this.nextOpeningTime = null;
    }

    public StoreClosedException(String message, LocalDateTime nextOpeningTime) {
        super(message, "STORE_CLOSED");
        this.nextOpeningTime = nextOpeningTime;
    }

    public LocalDateTime getNextOpeningTime() {
        return nextOpeningTime;
    }
}
