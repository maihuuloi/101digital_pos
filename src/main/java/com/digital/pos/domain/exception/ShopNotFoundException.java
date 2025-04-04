package com.digital.pos.domain.exception;

import java.util.UUID;

public class ShopNotFoundException extends RuntimeException{
  public ShopNotFoundException(UUID shopId) {
    super("Shop not found: " + shopId);
  }
}
