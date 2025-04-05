package com.digital.pos.adapter.out.menu;

import java.util.UUID;

public record MenuItemResponse(
    UUID menuItemId,
    String name,
    double price,
    boolean available
) {

}
