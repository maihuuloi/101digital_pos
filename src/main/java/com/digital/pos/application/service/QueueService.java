package com.digital.pos.application.service;

import com.digital.pos.adapter.in.rest.model.QueueInfo;
import com.digital.pos.adapter.in.rest.model.QueueOrderSummary;
import com.digital.pos.adapter.in.rest.model.QueueOrderSummary.StatusEnum;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService implements GetQueueSnapshotUseCase {

  private final ShopService shopService;
  private final OrderRepository orderRepository;
  private final QueueAssignmentEngine queueAssignmentEngine;

  private static List<QueueInfo> groupOrdersByQueue(List<Order> orders, Map<Integer, Integer> capacities) {
    Map<Integer, List<Order>> queueMap = orders.stream()
        .collect(Collectors.groupingBy(Order::getQueueNumber));

    List<QueueInfo> queueInfos = queueMap.entrySet().stream()
        .map(entry -> {
          Integer queueNumber = entry.getKey();
          List<Order> queueOrders = entry.getValue();
          List<Order> sortedOrder = queueOrders.stream()
              .sorted(Comparator.comparing(Order::getId))
              .toList();

          List<QueueOrderSummary> orderSummaries = mapOrdersToQueueOrderSummaries(
              sortedOrder);
          QueueInfo queueInfo = createQueueInfo(queueNumber, orderSummaries, capacities.get(queueNumber));

          return queueInfo;
        })
        .toList();
    return queueInfos;
  }

  private static List<QueueOrderSummary> mapOrdersToQueueOrderSummaries(List<Order> sortedOrder) {
    List<QueueOrderSummary> orderSummaries = new ArrayList<>();
    for (int i = 0; i < sortedOrder.size(); i++) {
      Order order = sortedOrder.get(i);
      QueueOrderSummary summary = createQueueSummary(order, i);
      orderSummaries.add(summary);

    }
    return orderSummaries;
  }

  private static QueueInfo createQueueInfo(Integer queueNumber, List<QueueOrderSummary> orderSummaries,
      Integer maxSize) {
    QueueInfo queueInfo = new QueueInfo();
    queueInfo.setQueueNumber(queueNumber);
    queueInfo.setSize(orderSummaries.size());
    queueInfo.setMaxSize(maxSize);
    queueInfo.setOrders(orderSummaries);
    return queueInfo;
  }

  private static QueueOrderSummary createQueueSummary(Order order, int i) {
    QueueOrderSummary summary = new QueueOrderSummary();
    summary.setOrderId(order.getId());
    summary.status(StatusEnum.fromValue(order.getStatus().name()));
    summary.setLivePosition(i + 1);
    return summary;
  }

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
  @Cacheable(value = "shop-queue-snapshot", key = "#shopId")
  public ShopQueueResponse getShopQueueSnapshot(UUID shopId) {
    log.info("Fetching queue snapshot for shop {}", shopId);
    validateShopExists(shopId);

    ShopConfiguration shopConfig = shopService.getShopConfig(shopId);
    Map<Integer, Integer> capacities = shopConfig.queueCapacities();

    List<Order> orders = orderRepository.findByShopIdAndStatus(shopId, OrderStatus.WAITING);

    List<QueueInfo> queueInfos = groupOrdersByQueue(orders, capacities);
    return new ShopQueueResponse(shopId, queueInfos);
  }

  private void validateShopExists(UUID shopId) {
    if (!shopService.existsById(shopId)) {
      throw new ShopNotFoundException(shopId);
    }
  }
}
