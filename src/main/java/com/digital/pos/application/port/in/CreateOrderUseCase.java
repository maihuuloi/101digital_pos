package com.digital.pos.application.port.in;


import com.digital.pos.adapter.in.rest.model.CreateOrderRequest;
import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;

public interface CreateOrderUseCase {

  OrderCreatedResponse createOrder(CreateOrderRequest request);

}
