package com.digital.pos.domain.service.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
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

    // Simulate pending orders:
    List<Order> pendingOrders = List.of(
        createOrder(1), createOrder(1), // 2 in queue 1
        createOrder(2), createOrder(2), createOrder(2), // 3 in queue 2
        createOrder(3), createOrder(3), createOrder(3), createOrder(3) // 4 in queue 3
    );

    // Queue 1 has 3 slots
    // Queue 2 has 5 slots
    // Queue 3 has 2 slots

    Order newOrder = createOrder(null); // unassigned

    QueueAssignmentResult result = strategy.assign(newOrder, config, pendingOrders);

    assertEquals(2, result.queueNumber());
    assertEquals(4, result.position()); // 3 existing orders → position 4
  }

  @Test
  void assign_shouldThrow_whenNoQueueHasAvailableSlot() {
    Map<Integer, Integer> capacities = Map.of(1, 2, 2, 2);
    ShopConfiguration config = new ShopConfiguration(UUID.randomUUID(), "MOST_AVAILABLE", capacities);

    List<Order> pendingOrders = List.of(
        createOrder(1), createOrder(1),
        createOrder(2), createOrder(2)
    );

    Order newOrder = createOrder(null);

    IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
        strategy.assign(newOrder, config, pendingOrders)
    );

    assertEquals("No available queues found for order assignment", ex.getMessage());
  }

  private Order createOrder(Integer queueNumber) {
    Order order = mock(Order.class);
    when(order.getQueueNumber()).thenReturn(queueNumber);
    return order;
  }
}
