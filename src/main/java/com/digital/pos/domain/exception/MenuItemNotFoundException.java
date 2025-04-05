package com.digital.pos.domain.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class MenuItemNotFoundException extends RuntimeException{

  private final UUID menuItemId;

  public MenuItemNotFoundException(UUID itemId) {
    super("Menu item not found in shop: " + itemId);
    this.menuItemId = itemId;
  }
}
