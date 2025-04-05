package com.digital.pos.application.service;

import com.digital.pos.adapter.in.rest.model.QueueInfo;
import com.digital.pos.adapter.in.rest.model.QueueOrderSummary;
import com.digital.pos.adapter.in.rest.model.ShopQueueResponse;
import com.digital.pos.application.port.in.GetQueueSnapshotUseCase;
import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.exception.ShopNotFoundException;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentContext;
import com.digital.pos.domain.service.QueueAssignmentEngine;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueService implements GetQueueSnapshotUseCase {

  private final ShopService shopService;
  private final OrderRepository orderRepository;
  private final QueueAssignmentEngine queueAssignmentEngine;
  public QueueAssignmentResult assignOrderToQueue(Order order) {
      return assign(order);
  }

  private QueueAssignmentResult assign(Order order) {
    log.debug("Assigning order {} to queue", order.getId());

    ShopConfiguration config = shopService.getShopConfig(order.getShopId());

    List<Order> waiting = orderRepository.findByShopIdAndStatus(order.getShopId(), OrderStatus.WAITING);
    QueueAssignmentResult assign = queueAssignmentEngine.assign(new QueueAssignmentContext(order, config, waiting));

    log.info("Order {} assigned to queue {}", order.getId(), assign.queueNumber());

    return assign;
  }

  public int getLivePosition(Order order) {
    return orderRepository.findPositionInQueueOrderById(order.getId());
  }

  @Override
  public ShopQueueResponse getShopQueueSnapshot(UUID shopId) {
    log.info("Fetching queue snapshot for shop {}", shopId);
    // Validate shop exists
    if (!shopService.existsById(shopId)) {
      throw new ShopNotFoundException(shopId);
    }
    // Fetch shop configuration
    ShopConfiguration shopConfig = shopService.getShopConfig(shopId);
    Map<Integer, Integer> capacities = shopConfig.queueCapacities();
    // Fetch all orders for the shop
    List<Order> orders = orderRepository.findByShopIdAndStatus(shopId, OrderStatus.WAITING);

    Map<Integer, List<Order>> queueMap = orders.stream()
        .collect(Collectors.groupingBy(Order::getQueueNumber));
    // Map orders to response
    List<QueueInfo> queueInfos = queueMap.entrySet().stream()
        .map(entry -> {
          Integer queueNumber = entry.getKey();
          List<Order> queueOrders = entry.getValue();

          List<Order> sortedOrder = queueOrders.stream()
              .sorted(Comparator.comparing(Order::getId))
              .toList();
          List<QueueOrderSummary> orderSummaries = new ArrayList<>();
          for (int i = 0; i < sortedOrder.size(); i++) {
            Order order = sortedOrder.get(i);
            QueueOrderSummary summary = new QueueOrderSummary();
            summary.setOrderId(order.getId());

            summary.setLivePosition(i+1);
            orderSummaries.add(summary);

          }

          QueueInfo queueInfo = new QueueInfo();
          queueInfo.setQueueNumber(queueNumber);
          queueInfo.setSize(orderSummaries.size());
          queueInfo.setMaxSize(capacities.get(queueNumber));
          queueInfo.setOrders(orderSummaries);

          return queueInfo;
        })
        .toList();

    return new ShopQueueResponse(shopId, queueInfos);
  }
}
