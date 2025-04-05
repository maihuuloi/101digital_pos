package com.digital.pos.domain.service.strategy;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentContext;
import java.util.List;

public class VipMemberShipQueueStrategy implements QueueAssignmentStrategy {

  @Override
  public boolean supports(ShopConfiguration config) {
    // Check if the strategy is applicable based on the shop configuration
    return config.queueStrategy().equalsIgnoreCase(StrategyType.VIP_MEMBERSHIP.name());
  }

  @Override
  public QueueAssignmentResult assign(QueueAssignmentContext queueAssignmentContext) {
    Order order = queueAssignmentContext.order();
    List<Order> waitingOrders = queueAssignmentContext.waitingOrders();

    return new QueueAssignmentResult(1);

  }

}
