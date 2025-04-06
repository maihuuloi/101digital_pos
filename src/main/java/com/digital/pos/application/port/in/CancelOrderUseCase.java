package com.digital.pos.application.port.in;

import java.util.UUID;

public interface CancelOrderUseCase {

  public UUID cancelOrder(Long orderId);
}
