package com.digital.pos.application.service;

import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentContext;
import com.digital.pos.domain.service.QueueAssignmentEngine;
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
      return assign(order);
  }

  private QueueAssignmentResult assign(Order order) {
    log.debug("Assigning order {} to queue", order.getId());

    ShopConfiguration config = shopService.getShopConfig(order.getShopId());

    List<Order> pending = orderRepository.findByShopIdAndStatus(order.getShopId(), OrderStatus.WAITING);
    QueueAssignmentResult assign = queueAssignmentEngine.assign(new QueueAssignmentContext(order, config, pending));

    log.info("Order {} assigned to queue {}", order.getId(), assign.queueNumber());

    return assign;
  }

  public Integer getLivePosition(Order savedOrder) {

    return orderRepository.findLivePositionInQueue(savedOrder.getId()) + 1;
  }
}
