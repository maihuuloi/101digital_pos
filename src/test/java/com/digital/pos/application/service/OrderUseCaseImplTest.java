package com.digital.pos.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseImplTest {

  @Mock
  private ShopService shopService;
  @Mock
  private MenuService menuService;
  @Mock
  private LockService lock;
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private OrderMapper orderMapper;
  @Mock
  private QueueService queueService;

  @InjectMocks
  private OrderService orderUseCase;

  @Test
  void createOrder_shouldSucceed_whenShopAndItemsAreValid() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    UUID menuItemId = UUID.randomUUID();
    int livePosition = 3;

    CreateOrderRequest request = new CreateOrderRequest(shopId,List.of(new OrderItemRequest(menuItemId, 2)));

    MenuItem menuItem = new MenuItem(menuItemId, "Latte", 50.0, true);
    Order order = Order.createNew(shopId, List.of(new OrderItem(menuItemId, 2, 50.0)));
    Order savedOrder = Order.createNew(shopId, order.getItems());
    savedOrder.assignQueue(1); // queueNumber
    ReflectionTestUtils.setField(savedOrder, "id", order.getId());

    OrderCreatedResponse expectedResponse = new OrderCreatedResponse(order.getId(), 1,livePosition,22);

    // Mocks
    when(shopService.existsById(shopId)).thenReturn(true);
    when(menuService.getAvailableItemIds(shopId)).thenReturn(Set.of(menuItem));
    when(lock.doWithLock(any(), any(), any(), any()))
        .thenAnswer(invocation -> ((Supplier<Order>) invocation.getArgument(3)).get());

    when(queueService.assignOrderToQueue(any())).thenReturn(new QueueAssignmentResult( 1));
    when(orderRepository.save(any())).thenReturn(savedOrder);
    when(queueService.getLivePosition(savedOrder)).thenReturn(livePosition);
    when(orderMapper.toOrderCreatedResponse(savedOrder, livePosition)).thenReturn(expectedResponse);

    // Act
    OrderCreatedResponse result = orderUseCase.createOrder(request);

    // Assert
    assertThat(result).isEqualTo(expectedResponse);
    verify(shopService).existsById(shopId);
    verify(menuService).getAvailableItemIds(shopId);
    verify(lock).doWithLock(eq(QueueLockKey.of(shopId)), any(), any(), any());
    verify(orderRepository).save(any());
    verify(queueService).assignOrderToQueue(any());
    verify(queueService).getLivePosition(savedOrder);
    verify(orderMapper).toOrderCreatedResponse(savedOrder, livePosition);
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
        queueService,
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
    verifyNoInteractions(queueService, orderRepository, orderMapper);
  }
  // createOrder_shouldPersistOrder_whenValidRequestGiven
  // createOrder_shouldAssignOrderToQueue_whenValidRequestGiven
  // createOrder_shouldReturnResponseWithCorrectDetails_whenOrderIsCreated

  @Test
  void serveOrder_shouldMarkOrderAsServed_whenOrderIsWaiting() {
    // Given
    Long orderId = 123L;
    UUID shopId = UUID.randomUUID();

    // Create a mock WAITING order
    Order order = Order.createNew(shopId, List.of());
    ReflectionTestUtils.setField(order, "id", orderId); // simulate saved order
    order.assignQueue(1); // required before serving

    assertThat(order.isWaiting()).isTrue(); // sanity check

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    when(lock.doWithLock(
        eq(QueueLockKey.of(shopId)),
        any(),
        any(),
        any()
    )).thenAnswer(invocation -> {
      Supplier<?> supplier = invocation.getArgument(3);
      return supplier.get();
    });

    // When
    orderUseCase.serveOrder(orderId);

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SERVED);
    verify(orderRepository).findById(orderId);
    verify(orderRepository).save(order);
    verify(lock).doWithLock(
        eq(QueueLockKey.of(shopId)),
        any(),
        any(),
        any()
    );
  }

  @Test
  void serveOrder_shouldThrowException_whenOrderNotFound() {
    // Given
    Long orderId = 123L;

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> orderUseCase.serveOrder(orderId))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("Order not found");

    verify(orderRepository).findById(orderId);
    verifyNoMoreInteractions(orderRepository, lock);
  }

  @Test
  void serveOrder_shouldThrowException_whenOrderIsNotWaiting() {
    // Given
    Long orderId = 456L;
    UUID shopId = UUID.randomUUID();

    Order order = Order.createNew(shopId, List.of());
    order.assignQueue(1);
    order.markAsServed(); // simulate status is already SERVED
    ReflectionTestUtils.setField(order, "id", orderId);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    // When / Then
    assertThatThrownBy(() -> orderUseCase.serveOrder(orderId))
        .isInstanceOf(InvalidOrderStateException.class)
        .hasMessageContaining("is in state SERVED");

    verify(orderRepository).findById(orderId);
    verifyNoMoreInteractions(orderRepository, lock);
  }
}
