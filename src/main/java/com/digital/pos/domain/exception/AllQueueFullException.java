package com.digital.pos.domain.exception;

import java.util.UUID;

public class AllQueueFullException extends RuntimeException {

  public AllQueueFullException(UUID shopId) {
    super("All queues are full in shop: " + shopId.toString());
  }
}
