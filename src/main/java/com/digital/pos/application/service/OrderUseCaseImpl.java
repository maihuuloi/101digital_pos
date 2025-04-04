package com.digital.pos.application.service;

import com.digital.pos.adapter.in.rest.model.CreateOrderRequest;
import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import com.digital.pos.application.mapper.OrderMapper;
import com.digital.pos.application.port.in.OrderUseCase;
import com.digital.pos.application.port.out.MenuService;
import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.exception.MenuItemNotFoundException;
import com.digital.pos.domain.exception.ShopNotFoundException;
import com.digital.pos.domain.model.MenuItem;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderItem;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.service.QueueAssignmentEngine;
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

  @Override
  public OrderCreatedResponse createOrder(CreateOrderRequest request) {
    UUID shopId = request.getShopId();

    // Validate shop exists
    if (!shopService.existsById(shopId)) {
      throw new ShopNotFoundException(shopId);
    }

    // Convert and validate each item
    Set<MenuItem> validMenuItemIds = menuService.getAvailableItemIds(shopId);
    Map<UUID,MenuItem> menuItemMap = validMenuItemIds.stream()
        .collect(Collectors.toMap(MenuItem::id, item -> item));

    List<OrderItem> items = request.getItems().stream()
        .map(itemRequest -> {
          UUID menuItemId = itemRequest.getMenuItemId();
          if (!menuItemMap.containsKey(menuItemId)) {
            throw new MenuItemNotFoundException(menuItemId);
          }
          MenuItem menuItem = menuItemMap.get(menuItemId);
          return new OrderItem(menuItemId, itemRequest.getQuantity(), menuItem.price());
        })
        .collect(Collectors.toList());

    // Create and assign
    Order order = Order.createNew(shopId, items);
    QueueAssignmentResult assignment = processOrderApplicationService.assignOrderToQueue(order);
    order.assignQueue(assignment.queueNumber(), assignment.position());

    // Persist
    Order savedOrder = orderRepository.save(order);
    return orderMapper.toOrderCreatedResponse(savedOrder);
  }
}
