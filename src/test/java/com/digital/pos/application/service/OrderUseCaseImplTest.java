package com.digital.pos.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import com.digital.coffeeshop.model.CreateOrderRequest;
import com.digital.coffeeshop.model.OrderCreatedResponse;
import com.digital.coffeeshop.model.OrderItemRequest;
import com.digital.pos.application.mapper.OrderMapper;
import com.digital.pos.application.port.out.MenuService;
import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.exception.MenuItemNotFoundException;
import com.digital.pos.domain.exception.ShopNotFoundException;
import com.digital.pos.domain.model.MenuItem;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderItem;
import com.digital.pos.domain.model.OrderStatus;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.service.QueueAssignmentEngine;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseImplTest {

  @Mock private OrderRepository orderRepository;
  @Mock private QueueAssignmentEngine queueAssignmentEngine;
  @Mock private ShopService shopService;
  @Mock private MenuService menuService;
  @Mock private OrderMapper orderMapper;

  @InjectMocks private OrderUseCaseImpl orderUseCase;

  @Test
  void createOrder_shouldSucceed_whenShopAndItemsAreValid() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    UUID item1 = UUID.randomUUID();
    UUID item2 = UUID.randomUUID();

    // Input request
    CreateOrderRequest request = new CreateOrderRequest();
    request.setShopId(shopId);
    request.setItems(List.of(
        new OrderItemRequest(item1, 2),
        new OrderItemRequest(item2, 1)
    ));

    // Shop validation
    when(shopService.existsById(shopId)).thenReturn(true);

    // Menu item enrichment
    Set<MenuItem> menuItems = Set.of(
        new MenuItem(item1, "Espresso", 30.0, true),
        new MenuItem(item2, "Croissant", 25.0, true)
    );
    when(menuService.getAvailableItemIds(shopId)).thenReturn(menuItems);

    // Queue assignment
    QueueAssignmentResult assignment = new QueueAssignmentResult(1, 3);
    when(queueAssignmentEngine.assignToQueue(any(Order.class))).thenReturn(assignment);

    // Domain Order created and saved
    Order savedOrder = new Order();
    savedOrder.setId(UUID.randomUUID());
    savedOrder.setShopId(shopId);
    savedOrder.setItems(List.of(
        new OrderItem(item1, 2, 30.0),
        new OrderItem(item2, 1, 25.0)
    ));
    savedOrder.assignQueue(1, 3);
    savedOrder.setStatus(OrderStatus.PENDING);

    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

    // Response
    OrderCreatedResponse expectedResponse = new OrderCreatedResponse()
        .orderId(savedOrder.getId())
        .queueNumber(1)
        .position(3)
        .estimatedWaitMinutes(4);

    when(orderMapper.toOrderCreatedResponse(savedOrder)).thenReturn(expectedResponse);

    // Act
    OrderCreatedResponse actualResponse = orderUseCase.createOrder(request);

    // Assert
    assertNotNull(actualResponse);
    assertEquals(expectedResponse.getOrderId(), actualResponse.getOrderId());
    assertEquals(expectedResponse.getQueueNumber(), actualResponse.getQueueNumber());
    assertEquals(expectedResponse.getPosition(), actualResponse.getPosition());
    assertEquals(expectedResponse.getEstimatedWaitMinutes(), actualResponse.getEstimatedWaitMinutes());

    // Verify interactions
    verify(shopService).existsById(shopId);
    verify(menuService).getAvailableItemIds(shopId);
    verify(queueAssignmentEngine).assignToQueue(any(Order.class));
    verify(orderRepository).save(any(Order.class));
    verify(orderMapper).toOrderCreatedResponse(savedOrder);
  }


  @Test
  void createOrder_shouldThrowException_whenShopIsNotFound() {
    // Arrange
    UUID nonExistentShopId = UUID.randomUUID();
    UUID menuItemId = UUID.randomUUID();

    CreateOrderRequest request = new CreateOrderRequest();
    request.setShopId(nonExistentShopId);
    request.setItems(List.of(new OrderItemRequest(menuItemId, 1)));

    // Mock behavior: shop does not exist
    when(shopService.existsById(nonExistentShopId)).thenReturn(false);

    // Act & Assert
    ShopNotFoundException exception = assertThrows(
        ShopNotFoundException.class,
        () -> orderUseCase.createOrder(request)
    );

    assertEquals("Shop not found: " + nonExistentShopId, exception.getMessage());

    // Verify no further interactions
    verify(menuService, never()).getAvailableItemIds(any());
    verify(orderRepository, never()).save(any());
    verify(queueAssignmentEngine, never()).assignToQueue(any());
    verify(orderMapper, never()).toOrderCreatedResponse(any());
  }
  @Test
  void createOrder_shouldThrowException_whenMenuItemIsInvalid() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    UUID validMenuItemId = UUID.randomUUID();
    UUID invalidMenuItemId = UUID.randomUUID(); // This will be considered invalid

    // Build the request with one valid and one invalid menu item.
    CreateOrderRequest request = new CreateOrderRequest();
    request.setShopId(shopId);
    request.setItems(List.of(
        new OrderItemRequest(validMenuItemId, 1),
        new OrderItemRequest(invalidMenuItemId, 2)
    ));

    // Mock shop existence
    when(shopService.existsById(shopId)).thenReturn(true);

    // Simulate menu service returning only the valid menu item.
    Set<MenuItem> availableMenuItems = Set.of(
        new MenuItem(validMenuItemId, "Espresso", 3.0, true)
    );
    when(menuService.getAvailableItemIds(shopId)).thenReturn(availableMenuItems);

    // Act & Assert: The call should throw a MenuItemNotFoundException for the invalidMenuItemId.
    MenuItemNotFoundException exception = assertThrows(
        MenuItemNotFoundException.class,
        () -> orderUseCase.createOrder(request)
    );
    assertEquals("Menu item not found: " + invalidMenuItemId, exception.getMessage());

    // Verify that no order is persisted and no further processing is done.
    verify(orderRepository, never()).save(any());
    verify(queueAssignmentEngine, never()).assignToQueue(any());
    verify(orderMapper, never()).toOrderCreatedResponse(any());
  }

  // createOrder_shouldPersistOrder_whenValidRequestGiven
  // createOrder_shouldAssignOrderToQueue_whenValidRequestGiven
  // createOrder_shouldReturnResponseWithCorrectDetails_whenOrderIsCreated
}
