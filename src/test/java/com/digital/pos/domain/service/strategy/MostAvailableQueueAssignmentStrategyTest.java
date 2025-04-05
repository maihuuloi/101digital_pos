package com.digital.pos.domain.service.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.digital.pos.domain.exception.AllQueueFullException;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.QueueAssignmentContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MostAvailableQueueAssignmentStrategyTest {

  private MostAvailableQueueAssignmentStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new MostAvailableQueueAssignmentStrategy();
  }

  @Test
  void assign_shouldReturnQueueWithMostAvailableSlots() {
    // Setup shop config: 3 queues
    Map<Integer, Integer> capacities = Map.of(
        1, 5,
        2, 8,
        3, 6
    );
    ShopConfiguration config = new ShopConfiguration(UUID.randomUUID(), "MOST_AVAILABLE", capacities);

    // Simulate waiting orders:
    List<Order> waitingOrders = List.of(
        createOrder(1), createOrder(1), // 2 in queue 1
        createOrder(2), createOrder(2), createOrder(2), // 3 in queue 2
        createOrder(3), createOrder(3), createOrder(3), createOrder(3) // 4 in queue 3
    );

    // Queue 1 has 3 slots
    // Queue 2 has 5 slots
    // Queue 3 has 2 slots

    Order newOrder = createOrder(null); // unassigned

    QueueAssignmentResult result = strategy.assign(new QueueAssignmentContext(newOrder, config, waitingOrders));

    assertEquals(2, result.queueNumber());
  }

  @Test
  void assign_shouldThrow_whenNoQueueHasAvailableSlot() {
    // Given
    UUID shopId = UUID.randomUUID();
    Order order = Order.createNew(shopId, List.of());

    Map<Integer, Integer> queueCapacities = Map.of(
        1, 2,
        2, 2
    );
    ShopConfiguration config = new ShopConfiguration(shopId, "most-available", queueCapacities);

    // Simulate all queues full
    List<Order> waitingOrders = List.of(
        orderWith(shopId, 1),
        orderWith(shopId, 1),
        orderWith(shopId, 2),
        orderWith(shopId, 2)
    );

    QueueAssignmentContext context = new QueueAssignmentContext(order, config, waitingOrders);

    // When / Then
    assertThrows(AllQueueFullException.class, () -> strategy.assign(context));
  }

  @Test
  void assign_shouldHandleEmptyWaitingOrders() {
    // Setup shop config: 3 queues
    Map<Integer, Integer> capacities = Map.of(
        1, 5,
        2, 8,
        3, 6
    );
    ShopConfiguration config = new ShopConfiguration(UUID.randomUUID(), "MOST_AVAILABLE", capacities);

    // No waiting orders
    List<Order> waitingOrders = List.of();

    Order newOrder = createOrder(null); // unassigned

    QueueAssignmentResult result = strategy.assign(new QueueAssignmentContext(newOrder, config, waitingOrders));

    assertEquals(2, result.queueNumber());
  }

  @Test
  void assign_shouldHandleSingleQueue() {
    // Setup shop config: 1 queue
    Map<Integer, Integer> capacities = Map.of(
        1, 5
    );
    ShopConfiguration config = new ShopConfiguration(UUID.randomUUID(), "MOST_AVAILABLE", capacities);

    // Simulate waiting orders:
    List<Order> waitingOrders = List.of(
        createOrder(1), createOrder(1) // 2 in queue 1
    );

    // Queue 1 has 3 slots

    Order newOrder = createOrder(null); // unassigned

    QueueAssignmentResult result = strategy.assign(new QueueAssignmentContext(newOrder, config, waitingOrders));

    assertEquals(1, result.queueNumber());
  }

  private Order orderWith(UUID shopId, int queueNumber) {
    Order o = Order.createNew(shopId, List.of());
    o.assignQueue(queueNumber);
    return o;
  }


  private Order createOrder(Integer queueNumber) {
    Order order = mock(Order.class);
    when(order.getQueueNumber()).thenReturn(queueNumber);
    return order;
  }

}
