package com.digital.pos.domain.service;

import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.strategy.QueueAssignmentStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueAssignmentEngineImpl implements QueueAssignmentEngine {

  private final List<QueueAssignmentStrategy> strategies;

  @Override
  public QueueAssignmentResult assign(Order order, ShopConfiguration config, List<Order> pendingOrders) {
    return strategies.stream()
        .filter(s -> s.supports(config))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No matching strategy for shop"))
        .assign(order,config, pendingOrders);
  }
}
