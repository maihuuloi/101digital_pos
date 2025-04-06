package com.digital.pos.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.digital.pos.adapter.in.rest.model.CreateOrderRequest;
import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import com.digital.pos.adapter.in.rest.model.OrderItemRequest;
import com.digital.pos.adapter.in.rest.model.OrderStatusResponse;
import com.digital.pos.adapter.in.rest.model.OrderStatusResponse.StatusEnum;
import com.digital.pos.application.mapper.OrderItemMapper;
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
class OrderServiceTest {

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
  @Mock
  private OrderItemMapper orderItemMapper;

  @InjectMocks
  private OrderService orderService;

  @Test
  void createOrder_shouldSucceed_whenShopAndItemsAreValid() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    UUID menuItemId = UUID.randomUUID();
    int livePosition = 3;

    CreateOrderRequest request = new CreateOrderRequest(shopId, List.of(new OrderItemRequest(menuItemId, 2)));

    MenuItem menuItem = new MenuItem(menuItemId, "Latte", 50.0, true);
    Order order = Order.createNew(shopId, List.of(new OrderItem(1l, menuItemId, 2, 50.0)));
    Order savedOrder = Order.createNew(shopId, order.getItems());
    savedOrder.assignQueue(1); // queueNumber
    ReflectionTestUtils.setField(savedOrder, "id", order.getId());

    OrderCreatedResponse expectedResponse = new OrderCreatedResponse(order.getId(), 1, livePosition, 22);

    // Mocks
    when(shopService.existsById(shopId)).thenReturn(true);
    when(menuService.getAvailableItemIds(shopId)).thenReturn(Set.of(menuItem));
    when(lock.doWithLock(any(), any(), any(), any()))
        .thenAnswer(invocation -> ((Supplier<Order>) invocation.getArgument(3)).get());

    when(queueService.assignOrderToQueue(any())).thenReturn(new QueueAssignmentResult(1));
    when(orderRepository.save(any())).thenReturn(savedOrder);
    when(queueService.getLivePosition(savedOrder)).thenReturn(livePosition);
    when(orderMapper.toOrderCreatedResponse(savedOrder, livePosition)).thenReturn(expectedResponse);

    // Act
    OrderCreatedResponse result = orderService.createOrder(request);

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
      orderService.createOrder(request);
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
      orderService.createOrder(request);
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
    orderService.serveOrder(orderId);

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
    assertThatThrownBy(() -> orderService.serveOrder(orderId))
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
    assertThatThrownBy(() -> orderService.serveOrder(orderId))
        .isInstanceOf(InvalidOrderStateException.class)
        .hasMessageContaining("is in state SERVED");

    verify(orderRepository).findById(orderId);
    verifyNoMoreInteractions(orderRepository, lock);
  }

  @Test
  void cancelOrder_shouldMarkOrderAsCanceled_whenOrderIsWaiting() {
    // Arrange
    Long orderId = 1L;
    UUID shopId = UUID.randomUUID();
    List<OrderItem> items = List.of(
        new OrderItem(1l, UUID.randomUUID(), 1, 20.0)
    );
    Order order = Order.createNew(shopId, items);
    order.assignQueue(1);

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
    given(lock.doWithLock(anyString(), any(), any(), any()))
        .willAnswer(invocation -> {
          Supplier<?> supplier = invocation.getArgument(3);
          return supplier.get();
        });

    // Act
    orderService.cancelOrder(orderId);

    // Assert
    assertEquals(OrderStatus.CANCELED, order.getStatus());
    verify(lock).doWithLock(eq(QueueLockKey.of(shopId)), any(), any(), any());
    verify(orderRepository).save(order);
  }

  @Test
  void cancelOrder_shouldThrowException_whenOrderIsNotWaiting() {
    // Arrange
    Long orderId = 2L;
    Order order = Order.createNew(UUID.randomUUID(), List.of());
    order.markAsServed();
    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

    // Act + Assert
    InvalidOrderStateException ex = assertThrows(
        InvalidOrderStateException.class,
        () -> orderService.cancelOrder(orderId)
    );

    assertTrue(ex.getMessage().contains("is in state"));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void cancelOrder_shouldThrowException_whenOrderDoesNotExist() {
    // Arrange
    Long orderId = 99L;
    given(orderRepository.findById(orderId)).willReturn(Optional.empty());

    // Act + Assert
    assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(orderId));
    verify(orderRepository, never()).save(any());
    verify(lock, never()).doWithLock(any(), any(), any(), any());
  }

  @Test
  void getOrder_shouldReturnStatusResponse_whenOrderExists() {
    // Arrange
    Long orderId = 1L;
    UUID shopId = UUID.randomUUID();
    OrderStatus orderStatus = OrderStatus.WAITING;

    List<OrderItem> orderItems = List.of(
        new OrderItem(1l, UUID.randomUUID(), 2, 10.0),
        new OrderItem(2l, UUID.randomUUID(), 1, 5.0)
    );
    Order order = Order.createNew(shopId, orderItems);
    order.assignQueue(2); // sets WAITING + queueNumber

    int livePosition = 3;
    double totalPrice = order.getTotalPrice();

    OrderStatusResponse expectedResponse = new OrderStatusResponse(
        order.getId(),
        shopId,
        StatusEnum.fromValue(orderStatus.name()),
        2,
        livePosition,
        7, // estimated wait minutes (null or mocked)
        totalPrice,
        List.of() // we’re not testing mapping here
    );

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
    given(orderRepository.findPositionInQueueOrderById(order.getId())).willReturn(livePosition);
    given(orderMapper.toOrderStatusResponse(eq(order), eq(livePosition), any()))
        .willReturn(expectedResponse);

    // Act
    OrderStatusResponse actualResponse = orderService.getOrder(orderId);

    // Assert
    assertEquals(expectedResponse, actualResponse);
    verify(orderRepository).findById(orderId);
    verify(orderRepository).findPositionInQueueOrderById(order.getId());
    verify(orderMapper).toOrderStatusResponse(eq(order), eq(livePosition), any());
  }
}
