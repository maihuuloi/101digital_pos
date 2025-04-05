package com.digital.pos.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import com.digital.pos.adapter.in.rest.model.CreateOrderRequest;
import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import com.digital.pos.adapter.in.rest.model.OrderItemRequest;
import com.digital.pos.application.mapper.OrderMapper;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseImplTest {

  @Mock
  private ShopService shopService;
  @Mock
  private MenuService menuService;
  @Mock
  private QueueAssignmentEngine queueAssignmentEngine;
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private OrderMapper orderMapper;
  @Mock
  private ProcessOrderApplicationService processOrderApplicationService;

  @InjectMocks
  private OrderUseCaseImpl orderUseCase;

  @Test
  void createOrder_shouldSucceed_whenShopAndItemsAreValid() {
    // Given
    UUID shopId = UUID.randomUUID();
    UUID menuItemId = UUID.randomUUID();

    OrderItemRequest itemRequest = new OrderItemRequest(menuItemId, 2);
    CreateOrderRequest request = new CreateOrderRequest(shopId, List.of(itemRequest));

    MenuItem menuItem = new MenuItem(menuItemId, "Latte", 30.0, true);
    Set<MenuItem> availableItems = Set.of(menuItem);

    OrderItem orderItem = new OrderItem(menuItemId, 2, 30.0);
    Order orderBeforeAssignment = Order.createNew(shopId, List.of(orderItem));
    QueueAssignmentResult assignment = new QueueAssignmentResult(1);

    Order orderAfterAssignment = Order.createNew(shopId, List.of(orderItem));
    orderAfterAssignment.assignQueue(1);

    Order savedOrder = Order.createNew(shopId, List.of(orderItem));
    savedOrder.assignQueue(1);

    OrderCreatedResponse expectedResponse = new OrderCreatedResponse(savedOrder.getId(), 1, 3, 6);

    // When
    when(shopService.existsById(shopId)).thenReturn(true);
    when(menuService.getAvailableItemIds(shopId)).thenReturn(availableItems);
    when(processOrderApplicationService.assignOrderToQueue(any(Order.class))).thenReturn(assignment);
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
    when(orderMapper.toOrderCreatedResponse(savedOrder)).thenReturn(expectedResponse);

    // Then
    OrderCreatedResponse result = orderUseCase.createOrder(request);

    assertNotNull(result);
    assertEquals(expectedResponse, result);

    // Verify interactions
    verify(shopService).existsById(shopId);
    verify(menuService).getAvailableItemIds(shopId);
    verify(processOrderApplicationService).assignOrderToQueue(any(Order.class));
    verify(orderRepository).save(any(Order.class));
    verify(orderMapper).toOrderCreatedResponse(savedOrder);
  }

  @Test
  void createOrder_shouldThrowException_whenShopIsNotFound() {
    // Given
    UUID shopId = UUID.randomUUID();
    UUID menuItemId = UUID.randomUUID();

    OrderItemRequest itemRequest = new OrderItemRequest(menuItemId, 1);
    CreateOrderRequest request = new CreateOrderRequest(shopId, List.of(itemRequest));

    when(shopService.existsById(shopId)).thenReturn(false);

    // When + Then
    ShopNotFoundException exception = assertThrows(ShopNotFoundException.class, () -> {
      orderUseCase.createOrder(request);
    });

    assertEquals(shopId, exception.getShopId());

    // Ensure no downstream services were called
    verify(shopService).existsById(shopId);
    verifyNoMoreInteractions(
        shopService,
        menuService,
        processOrderApplicationService,
        orderRepository,
        orderMapper
    );
  }

  @Test
  void createOrder_shouldThrowException_whenMenuItemIsInvalid() {
    // Given
    UUID shopId = UUID.randomUUID();
    UUID invalidMenuItemId = UUID.randomUUID(); // Item we request that does not exist
    UUID validMenuItemId = UUID.randomUUID();   // Only this one exists

    OrderItemRequest itemRequest = new OrderItemRequest(invalidMenuItemId, 1);
    CreateOrderRequest request = new CreateOrderRequest(shopId, List.of(itemRequest));

    Set<MenuItem> availableItems = Set.of(
        new MenuItem(validMenuItemId, "Espresso", 25.0, true)
    );

    when(shopService.existsById(shopId)).thenReturn(true);
    when(menuService.getAvailableItemIds(shopId)).thenReturn(availableItems);

    // When & Then
    MenuItemNotFoundException exception = assertThrows(MenuItemNotFoundException.class, () -> {
      orderUseCase.createOrder(request);
    });

    assertEquals(invalidMenuItemId, exception.getMenuItemId());

    // Verify interactions
    verify(shopService).existsById(shopId);
    verify(menuService).getAvailableItemIds(shopId);
    verifyNoInteractions(processOrderApplicationService, orderRepository, orderMapper);
  }
  // createOrder_shouldPersistOrder_whenValidRequestGiven
  // createOrder_shouldAssignOrderToQueue_whenValidRequestGiven
  // createOrder_shouldReturnResponseWithCorrectDetails_whenOrderIsCreated

  @Test
  void serveOrder_shouldMarkOrderAsServed_whenOrderIsWaiting() {
    // Given
    Long orderId = UUID.randomUUID();
    Order mockOrder = mock(Order.class);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
    when(mockOrder.isWaiting()).thenReturn(true);

    // When
    orderUseCase.serveOrder(orderId);

    // Then
    verify(mockOrder).markAsServed();
    verify(orderRepository).save(mockOrder);
  }
}
