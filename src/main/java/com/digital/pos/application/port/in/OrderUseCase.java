package com.digital.pos.application.port.in;


import com.digital.pos.adapter.in.rest.model.CreateOrderRequest;
import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import java.util.UUID;

public interface OrderUseCase {
  OrderCreatedResponse createOrder(CreateOrderRequest request);

  void serveOrder(Long orderId);
}
