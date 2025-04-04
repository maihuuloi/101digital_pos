package com.digital.pos.domain.model;

import java.util.UUID;

public record MenuItem(UUID id,
                       String name,
                       double price,
                       boolean available) {
}
