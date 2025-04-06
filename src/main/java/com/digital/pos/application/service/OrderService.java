package com.digital.pos.application.service;

import com.digital.pos.adapter.in.rest.model.CreateOrderRequest;
import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import com.digital.pos.adapter.in.rest.model.OrderItemSummary;
import com.digital.pos.adapter.in.rest.model.OrderStatusResponse;
import com.digital.pos.application.mapper.OrderItemMapper;
import com.digital.pos.application.mapper.OrderMapper;
import com.digital.pos.application.port.in.CancelOrderUseCase;
import com.digital.pos.application.port.in.CreateOrderUseCase;
import com.digital.pos.application.port.in.GetOrderUseCase;
import com.digital.pos.application.port.in.ServeOrderUseCase;
import com.digital.pos.application.port.out.LockService;
import com.digital.pos.application.port.out.MenuService;
import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.exception.InvalidOrderStateException;
import com.digital.pos.domain.exception.MenuItemNotFoundException;
import com.digital.pos.domain.exception.OrderNotFoundException;
import com.digital.pos.domain.exception.ShopNotFoundException;
import com.digital.pos.domain.model.MenuItem;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderItem;
import com.digital.pos.domain.model.OrderStatus;
import com.digital.pos.domain.model.QueueAssignmentResult;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OrderService implements CreateOrderUseCase, ServeOrderUseCase, CancelOrderUseCase, GetOrderUseCase {

  private final OrderRepository orderRepository;
  private final QueueService queueService;
  private final ShopService shopService; // External service
  private final MenuService menuService; // External service
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;
  private final LockService lock;

  private static List<OrderItem> getValidOrderItems(CreateOrderRequest request, Map<UUID, MenuItem> menuItemMap) {
    return request.getItems().stream()
        .map(itemRequest -> {
          UUID menuItemId = itemRequest.getMenuItemId();
          if (!menuItemMap.containsKey(menuItemId)) {
            throw new MenuItemNotFoundException(menuItemId);
          }
          MenuItem menuItem = menuItemMap.get(menuItemId);
          return new OrderItem(null, menuItemId, itemRequest.getQuantity(), menuItem.price());
        })
        .collect(Collectors.toList());
  }

  private static void validateOrderStatus(Order order) {
    if (!order.isWaiting()) {
      log.warn("Cannot serve order {} because it is in status {}", order.getId(), order.getStatus());
      throw new InvalidOrderStateException(order.getId(), order.getStatus(), OrderStatus.WAITING);
    }
  }

  @Override
  @Transactional
  @CacheEvict(value = "shop-queue-snapshot", key = "#request.shopId")
  public OrderCreatedResponse createOrder(CreateOrderRequest request) {
    UUID shopId = request.getShopId();
    log.debug("Creating order for shop {}", shopId);

    validateShopExists(shopId);

    List<OrderItem> items = getValidOrderItems(request, shopId);
    Order order = Order.createNew(shopId, items);

    Order savedOrder = processOrderWithLock(shopId, order);

    Integer livePosition = queueService.getLivePosition(savedOrder);
    log.info("Order {} live position in queue is {}", savedOrder.getId(), livePosition);

    return orderMapper.toOrderCreatedResponse(savedOrder, livePosition);
  }

  private Order processOrderWithLock(UUID shopId, Order order) {
    String lockKey = QueueLockKey.of(shopId);
    return lock.doWithLock(lockKey,
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        () -> processOrder(order)
    );
  }

  private List<OrderItem> getValidOrderItems(CreateOrderRequest request, UUID shopId) {
    log.info("Fetching available menu items for shop {}", shopId);
    Set<MenuItem> shopMenuItems = menuService.getAvailableItemIds(shopId);
    Map<UUID, MenuItem> menuItemMap = shopMenuItems.stream()
        .collect(Collectors.toMap(MenuItem::id, item -> item));
    log.debug("Found {} available menu items", shopMenuItems.size());

    List<OrderItem> items = getValidOrderItems(request, menuItemMap);
    log.info("Order items validated, found {} items", items.size());

    return items;
  }

  private void validateShopExists(UUID shopId) {
    if (!shopService.existsById(shopId)) {
      throw new ShopNotFoundException(shopId);
    }
  }

  @Override
  @Transactional
  @CacheEvict(value = "shop-queue-snapshot", key = "#result")
  public UUID serveOrder(Long orderId) {
    log.info("Attempting to serve order {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    validateOrderStatus(order);

    order.markAsServed();

    serveOrderWithLock(orderId, order);

    return order.getShopId();
  }

  private Order processOrder(Order order) {
    QueueAssignmentResult assignment = queueService.assignOrderToQueue(order);
    order.assignQueue(assignment.queueNumber());

    Order savedOrder = orderRepository.save(order);
    log.debug("Saved order with ID {}", order.getId());

    return savedOrder;
  }

  private void serveOrderWithLock(Long orderId, Order order) {
    String lockKey = QueueLockKey.of(order.getShopId());

    lock.doWithLock(
        lockKey,
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        () -> {
          orderRepository.save(order);
          log.debug("Order {} successfully marked as SERVED", orderId);
          return null;
        }
    );
  }

  @Override
  public void cancelOrder(Long orderId) {
    log.info("Attempting to cancel order {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.isWaiting()) {
      log.warn("Cannot cancel order {} because it is in status {}", orderId, order.getStatus());
      throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.WAITING);
    }

    order.markAsCanceled();

    String lockKey = QueueLockKey.of(order.getShopId());

    lock.doWithLock(
        lockKey,
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        () -> {
          orderRepository.save(order);
          log.debug("Order {} successfully marked as CANCELED", orderId);
          return null;
        }
    );
  }

  @Override
  public OrderStatusResponse getOrder(Long orderId) {
    log.info("Fetching order status for ID {}", orderId);

    // 1. Find order
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    // 2. Validate the order is queue-based (i.e., has shop & queue number)
    if (order.getStatus() == OrderStatus.WAITING && order.getQueueNumber() == null) {
      throw new IllegalStateException("Order is in WAITING status but has no queue number assigned");
    }

    // 3. Calculate live position (dynamic)
    int livePosition = 0;
    if (order.getStatus() == OrderStatus.WAITING) {
      livePosition = orderRepository.findPositionInQueueOrderById(order.getId());
    }
    // 6. Estimate wait time (simplified logic)

    // 4. Map order items to DTO
    List<OrderItemSummary> itemSummaries = order.getItems().stream()
        .map(item -> new OrderItemSummary(
            item.menuItemId(),
            item.quantity(),
            item.price(),
            item.getTotalPrice()
        ))
        .toList();

    // 5. Calculate total price
    double totalPrice = itemSummaries.stream()
        .mapToDouble(OrderItemSummary::getTotalPrice)
        .sum();
    List<OrderItemSummary> orderItemSummaries = orderItemMapper.map(order.getItems());

    // 7. Return response
    OrderStatusResponse orderStatusResponse = orderMapper.toOrderStatusResponse(order, livePosition, null, totalPrice);
    orderStatusResponse.setItems(orderItemSummaries);
    return orderStatusResponse;
  }
}
