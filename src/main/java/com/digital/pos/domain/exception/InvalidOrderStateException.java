package com.digital.pos.domain.exception;

import com.digital.pos.domain.model.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {

  public InvalidOrderStateException(Long orderId, OrderStatus status, OrderStatus waiting) {

    super(String.format("Order with id %d is in state %s, but it should be in state %s", orderId, status, waiting));
  }
}
