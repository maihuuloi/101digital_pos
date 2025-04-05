package com.digital.pos.domain.service.strategy;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentContext;
import java.util.List;

public class VipMemberShipQueueStrategy implements QueueAssignmentStrategy {

  @Override
  public boolean supports(ShopConfiguration config) {
    return config.queueStrategy().equalsIgnoreCase(StrategyType.VIP_MEMBERSHIP.name());
  }

  @Override
  public QueueAssignmentResult assign(QueueAssignmentContext queueAssignmentContext) {
    Order order = queueAssignmentContext.order();
    List<Order> waitingOrders = queueAssignmentContext.waitingOrders();
    //TODO: Implement the logic to assign a queue based on highscore score
    return new QueueAssignmentResult(1);

  }

}
