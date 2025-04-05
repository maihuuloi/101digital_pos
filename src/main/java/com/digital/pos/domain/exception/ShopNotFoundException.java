package com.digital.pos.domain.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class ShopNotFoundException extends RuntimeException {

  private final UUID shopId;

  public ShopNotFoundException(UUID shopId) {
    super("Shop not found: " + shopId);
    this.shopId = shopId;
  }
}
