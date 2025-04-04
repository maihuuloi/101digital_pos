package com.digital.pos.domain.exception;

import java.util.UUID;

public class ShopConfigurationNotFoundException extends RuntimeException{
  public ShopConfigurationNotFoundException(UUID shopId) {
    super("Shop configuration not found: " + shopId);
  }
}
