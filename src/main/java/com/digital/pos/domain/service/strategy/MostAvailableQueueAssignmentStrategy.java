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

  @Override
  public boolean supports(ShopConfiguration config) {
    return StrategyType.MOST_AVAILABLE.name().equalsIgnoreCase(config.queueStrategy());
  }

  @Override
  public QueueAssignmentResult assign(QueueAssignmentContext ctx) {
    Map<Integer, Integer> capacities = ctx.config().queueCapacities();
    Map<Integer, Long> currentCounts = ctx.waitingOrders().stream()
        .collect(Collectors.groupingBy(Order::getQueueNumber, Collectors.counting()));

    int selectedQueue = findQueueWithMostAvailableSlots(capacities, currentCounts);

    if (selectedQueue == -1) {
      throw new AllQueueFullException(ctx.order().getShopId());
    }

    return new QueueAssignmentResult(selectedQueue);
  }

  private int findQueueWithMostAvailableSlots(Map<Integer, Integer> capacities, Map<Integer, Long> currentCounts) {
    int selectedQueue = -1;
    int maxAvailableSlots = 0;

    for (Map.Entry<Integer, Integer> entry : capacities.entrySet()) {
      int queueNumber = entry.getKey();
      int availableSlots = calculateAvailableSlots(entry.getValue(), currentCounts.getOrDefault(queueNumber, 0L));

      if (availableSlots > maxAvailableSlots) {
        maxAvailableSlots = availableSlots;
        selectedQueue = queueNumber;
      }
    }

    return selectedQueue;
  }

  private int calculateAvailableSlots(int maxCapacity, long currentLoad) {
    return maxCapacity - (int) currentLoad;
  }
}
