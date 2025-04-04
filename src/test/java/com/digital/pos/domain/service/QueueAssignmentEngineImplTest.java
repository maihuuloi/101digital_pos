package com.digital.pos.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.strategy.QueueAssignmentStrategy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueueAssignmentEngineImplTest {

  @Mock
  private QueueAssignmentStrategy shortestStrategy;

  @Mock
  private QueueAssignmentStrategy mostAvailableStrategy;

  private QueueAssignmentEngineImpl engine;

  @BeforeEach
  void setUp() {
    engine = new QueueAssignmentEngineImpl(List.of(shortestStrategy, mostAvailableStrategy));
  }

  @Test
  void assign_shouldUseMatchingStrategy_whenOneSupportsConfig() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    ShopConfiguration config = new ShopConfiguration(shopId, "SHORTEST_QUEUE", Map.of(1, 5, 2, 5, 3, 5));
    Order order = new Order(); // mock or real
    List<Order> pendingOrders = List.of();

    QueueAssignmentResult expectedResult = new QueueAssignmentResult(2, 3);

    when(shortestStrategy.supports(config)).thenReturn(true);
    when(shortestStrategy.assign(order, config, pendingOrders)).thenReturn(expectedResult);

    when(mostAvailableStrategy.supports(config)).thenReturn(false);

    // Act
    QueueAssignmentResult result = engine.assign(order, config, pendingOrders);

    // Assert
    assertEquals(expectedResult, result);
    verify(shortestStrategy).assign(order, config, pendingOrders);
    verifyNoInteractions(mostAvailableStrategy);
  }

  @Test
  void assign_shouldThrow_whenNoStrategySupportsConfig() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    ShopConfiguration config = new ShopConfiguration(shopId, "UNKNOWN", Map.of());
    Order order = new Order();
    List<Order> pendingOrders = List.of();

    when(shortestStrategy.supports(config)).thenReturn(false);
    when(mostAvailableStrategy.supports(config)).thenReturn(false);

    // Act & Assert
    IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
        engine.assign(order, config, pendingOrders)
    );

    assertEquals("No matching strategy for shop", ex.getMessage());
  }

}
