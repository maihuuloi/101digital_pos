package com.digital.pos.application.port.in;

import com.digital.pos.adapter.in.rest.model.OrderStatusResponse;

public interface GetOrderUseCase {

  OrderStatusResponse getOrder(Long orderId);
}
