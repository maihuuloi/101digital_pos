package com.digital.pos.application.port.out;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository  {

  Order save(Order any);

  List<Order> findByShopIdAndStatus(UUID shopId, OrderStatus orderStatus);

  Optional<Order> findById(Long orderId);

  Integer findLivePositionInQueue(Long orderId);
}
