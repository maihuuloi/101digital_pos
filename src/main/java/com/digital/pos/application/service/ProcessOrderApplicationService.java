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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessOrderApplicationService {

  private final ShopService shopService;
  private final OrderRepository orderRepository;
  private final QueueAssignmentEngine queueAssignmentEngine;

  public QueueAssignmentResult assignOrderToQueue(Order order) {
    log.debug("Assigning order {} to queue", order.getId());

    ShopConfiguration config = shopService.getShopConfig(order.getShopId());

    List<Order> pending = orderRepository.findByShopIdAndStatus(order.getShopId(), OrderStatus.PENDING);
    QueueAssignmentResult assign = queueAssignmentEngine.assign(order, config, pending);

    log.info("Order {} assigned to queue {}", order.getId(), assign.queueNumber());

    return assign;
  }
}
