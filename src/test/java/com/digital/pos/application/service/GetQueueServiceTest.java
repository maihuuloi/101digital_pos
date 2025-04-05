package com.digital.pos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.digital.pos.adapter.in.rest.model.QueueInfo;
import com.digital.pos.adapter.in.rest.model.QueueOrderSummary;
import com.digital.pos.adapter.in.rest.model.ShopQueueResponse;
import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.exception.ShopNotFoundException;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import com.digital.pos.domain.model.ShopConfiguration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

  @Mock
  private ShopService shopService;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private QueueService queueService;

  private final UUID shopId = UUID.randomUUID();

  @Test
  void shouldThrowExceptionWhenShopNotFound() {
    when(shopService.existsById(shopId)).thenReturn(false);

    assertThrows(ShopNotFoundException.class, () -> {
      queueService.getShopQueueSnapshot(shopId);
    });
  }
  @Test
  void shouldReturnSortedOrdersWithLivePositionBasedOnId() {
    when(shopService.existsById(shopId)).thenReturn(true);

    // Shop config with one queue of max size 5
    ShopConfiguration config = new ShopConfiguration(shopId, "ROUND_ROBIN", Map.of(1, 5));
    when(shopService.getShopConfig(shopId)).thenReturn(config);

    // Create 3 orders assigned to the same queue (queue 1)
    Order order1 = Order.createNew(shopId, List.of());
    order1.assignQueue(1);
    order1.setId(300L);

    Order order2 = Order.createNew(shopId, List.of());
    order2.assignQueue(1);
    order2.setId(100L);

    Order order3 = Order.createNew(shopId, List.of());
    order3.assignQueue(1);
    order3.setId(200L);

    // Return the orders (unordered)
    when(orderRepository.findByShopIdAndStatus(shopId, OrderStatus.WAITING))
        .thenReturn(List.of(order1, order2, order3));

    // Act
    ShopQueueResponse response = queueService.getShopQueueSnapshot(shopId);

    // Assert
    assertEquals(shopId, response.getShopId());
    assertEquals(1, response.getQueues().size());

    QueueInfo queue = response.getQueues().get(0);
    assertEquals(1, queue.getQueueNumber());
    assertEquals(3, queue.getSize());
    assertEquals(5, queue.getMaxSize());

    List<QueueOrderSummary> summaries = queue.getOrders();

    // Sorted by ID: 100 -> 200 -> 300
    assertEquals(100L, summaries.get(0).getOrderId());
    assertEquals(1, summaries.get(0).getLivePosition());

    assertEquals(200L, summaries.get(1).getOrderId());
    assertEquals(2, summaries.get(1).getLivePosition());

    assertEquals(300L, summaries.get(2).getOrderId());
    assertEquals(3, summaries.get(2).getLivePosition());
  }
}
