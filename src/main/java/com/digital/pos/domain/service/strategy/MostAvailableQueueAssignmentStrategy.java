package com.digital.pos.domain.service.strategy;

import com.digital.pos.domain.exception.AllQueueFullException;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentContext;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MostAvailableQueueAssignmentStrategy implements QueueAssignmentStrategy {

  public static final String NAME = "MOST_AVAILABLE";

  @Override
  public boolean supports(ShopConfiguration config) {
    return NAME.equalsIgnoreCase(config.queueStrategy());
  }

  public QueueAssignmentResult assign(QueueAssignmentContext queueAssignmentContext) {
    Map<Integer, Integer> capacities = queueAssignmentContext.config().queueCapacities();
    Map<Integer, Long> currentCounts = queueAssignmentContext.pendingOrders().stream()
        .collect(Collectors.groupingBy(Order::getQueueNumber, Collectors.counting()));

    int selectedQueue = -1;
    int maxAvailableSlots = Integer.MIN_VALUE;

    for (Map.Entry<Integer, Integer> entry : capacities.entrySet()) {
      int queueNumber = entry.getKey();
      int maxCapacity = entry.getValue();
      long currentLoad = currentCounts.getOrDefault(queueNumber, 0L);
      int availableSlots = maxCapacity - (int) currentLoad;

      if (availableSlots > maxAvailableSlots) {
        maxAvailableSlots = availableSlots;
        selectedQueue = queueNumber;
      }
    }

    if (selectedQueue == -1 || maxAvailableSlots <= 0) {
      throw new AllQueueFullException(queueAssignmentContext.order().getShopId());
    }

    int position = currentCounts.getOrDefault(selectedQueue, 0L).intValue() + 1;

    return new QueueAssignmentResult(selectedQueue);
  }
}
