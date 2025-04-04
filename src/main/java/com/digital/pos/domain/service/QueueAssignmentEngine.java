package com.digital.pos.domain.service;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import java.util.List;

public interface QueueAssignmentEngine {
  QueueAssignmentResult assign(Order order, ShopConfiguration config, List<Order> pendingOrders);
}
