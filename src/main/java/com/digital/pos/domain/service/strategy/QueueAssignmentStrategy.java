package com.digital.pos.domain.service.strategy;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import java.util.List;

public interface QueueAssignmentStrategy {
  boolean supports(ShopConfiguration config); // e.g., based on shopId or strategy type
  QueueAssignmentResult assign(Order order,ShopConfiguration config, List<Order> pendingOrders);
}
