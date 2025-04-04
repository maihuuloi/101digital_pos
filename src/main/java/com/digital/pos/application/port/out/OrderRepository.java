package com.digital.pos.application.port.out;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository  {

  Order save(Order any);

  List<Order> findByShopIdAndStatus(UUID shopId, OrderStatus orderStatus);
}
