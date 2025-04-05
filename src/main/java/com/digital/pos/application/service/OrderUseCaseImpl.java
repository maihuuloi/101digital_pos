package com.digital.pos.application.service;

import com.digital.pos.adapter.in.rest.model.CreateOrderRequest;
import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import com.digital.pos.application.mapper.OrderMapper;
import com.digital.pos.application.port.in.OrderUseCase;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OrderUseCaseImpl implements OrderUseCase {

  private final OrderRepository orderRepository;
  private final ProcessOrderApplicationService processOrderApplicationService;
  private final ShopService shopService; // External service
  private final MenuService menuService; // External service
  private final OrderMapper orderMapper;
  private final LockService lock;

  @Override
  @Transactional
  public OrderCreatedResponse createOrder(CreateOrderRequest request) {
    UUID shopId = request.getShopId();
    log.debug("Creating order for shop {}", shopId);

    // Validate shop exists
    if (!shopService.existsById(shopId)) {
      throw new ShopNotFoundException(shopId);
    }

    // Convert and validate each item
    log.info("Fetching available menu items for shop {}", shopId);
    Set<MenuItem> shopMenuItems = menuService.getAvailableItemIds(shopId);
    Map<UUID,MenuItem> menuItemMap = shopMenuItems.stream()
        .collect(Collectors.toMap(MenuItem::id, item -> item));
    log.debug("Found {} available menu items", shopMenuItems.size());

    List<OrderItem> items = getValidOrderItems(request, menuItemMap);
    log.info("Order items validated, found {} items", items.size());
    Order order = Order.createNew(shopId, items);


    String lockKey = QueueLockKey.of(shopId);
    Order savedOrder = lock.doWithLock(lockKey,
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        () -> processOrder(order)
    );
    log.debug("Order {} created and assigned to queue", savedOrder.getId());

    Integer livePosition = processOrderApplicationService.getLivePosition(savedOrder);
    log.info("Order {} live position in queue is {}", savedOrder.getId(), livePosition);

    return orderMapper.toOrderCreatedResponse(savedOrder, livePosition);
  }

  private Order processOrder(Order order) {
    // Create and assign
    QueueAssignmentResult assignment = processOrderApplicationService.assignOrderToQueue(order);
    order.assignQueue(assignment.queueNumber());

    Order savedOrder = orderRepository.save(order);
    log.debug("Saved order with ID {}", order.getId());

    return savedOrder;
  }

  private static List<OrderItem> getValidOrderItems(CreateOrderRequest request, Map<UUID, MenuItem> menuItemMap) {
    return request.getItems().stream()
        .map(itemRequest -> {
          UUID menuItemId = itemRequest.getMenuItemId();
          if (!menuItemMap.containsKey(menuItemId)) {
            throw new MenuItemNotFoundException(menuItemId);
          }
          MenuItem menuItem = menuItemMap.get(menuItemId);
          return new OrderItem(menuItemId, itemRequest.getQuantity(), menuItem.price());
        })
        .collect(Collectors.toList());
  }


  @Override
  public void serveOrder(Long orderId) {
    log.info("Attempting to serve order {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.isWaiting()) {
      log.warn("Cannot serve order {} because it is in status {}", orderId, order.getStatus());
      throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.WAITING);
    }

    order.markAsServed();
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

}
