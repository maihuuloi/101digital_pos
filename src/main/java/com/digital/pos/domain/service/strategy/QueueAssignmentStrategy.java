package com.digital.pos.domain.service.strategy;

import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentContext;

public interface QueueAssignmentStrategy {

  boolean supports(ShopConfiguration config); // e.g., based on shopId or strategy type

  QueueAssignmentResult assign(QueueAssignmentContext queueAssignmentContext);
}
