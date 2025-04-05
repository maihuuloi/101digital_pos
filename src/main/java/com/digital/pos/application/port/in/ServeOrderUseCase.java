package com.digital.pos.application.port.in;

import java.util.UUID;

public interface ServeOrderUseCase {

  UUID serveOrder(Long orderId);
}
