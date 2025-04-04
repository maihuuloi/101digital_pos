package com.digital.pos.domain.exception;

import java.util.UUID;

public class MenuItemNotFoundException extends RuntimeException{
  public MenuItemNotFoundException(UUID itemId) {
    super("Menu item not found in shop: " + itemId);
  }
}
