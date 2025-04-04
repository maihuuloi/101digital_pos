package com.digital.pos.domain.model;

import java.util.UUID;

public record OrderItem(
        UUID menuItemId,
        int quantity,
        double price
) {

    public OrderItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

    }

    public double getTotalPrice() {
        return quantity * price;
    }

}
