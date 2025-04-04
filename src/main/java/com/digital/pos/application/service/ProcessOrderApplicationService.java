package com.digital.pos.application.service;

import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentEngine;
import com.digital.pos.domain.service.strategy.QueueAssignmentStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class ProcessOrderApplicationService {

  private final ShopService shopService;
  private final OrderRepository orderRepository;
  private final QueueAssignmentEngine queueAssignmentEngine;

  public QueueAssignmentResult assignOrderToQueue(Order order) {
    ShopConfiguration config = shopService.getShopConfig(order.getShopId());
    List<Order> pending = orderRepository.findByShopIdAndStatus(order.getShopId(), OrderStatus.PENDING);

    return queueAssignmentEngine.assign(order, config, pending);
  }
}
