package com.digital.pos.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.model.ShopConfiguration;
import com.digital.pos.domain.service.strategy.QueueAssignmentStrategy;
import com.digital.pos.domain.service.strategy.StrategyType;
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
  private QueueAssignmentStrategy mostAvailableStrategy;

  private QueueAssignmentEngineImpl engine;

  @BeforeEach
  void setUp() {
    engine = new QueueAssignmentEngineImpl(List.of(mostAvailableStrategy));
  }

  @Test
  void assign_shouldUseMatchingStrategy_whenOneSupportsConfig() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    ShopConfiguration config = new ShopConfiguration(shopId,
        StrategyType.MOST_AVAILABLE.name(), Map.of(1, 5, 2, 5, 3, 5));
    Order order = new Order(); // mock or real
    List<Order> waitingOrders = List.of();

    QueueAssignmentResult expectedResult = new QueueAssignmentResult(2);

    when(mostAvailableStrategy.supports(config)).thenReturn(true);
    when(mostAvailableStrategy.assign(new QueueAssignmentContext(order, config, waitingOrders)))
        .thenReturn(expectedResult);

    // Act
    QueueAssignmentResult result = engine.assign(new QueueAssignmentContext(order, config, waitingOrders));

    // Assert
    assertEquals(expectedResult, result);
    verify(mostAvailableStrategy).assign(new QueueAssignmentContext(order, config, waitingOrders));
  }

  @Test
  void assign_shouldThrow_whenNoStrategySupportsConfig() {
    // Arrange
    UUID shopId = UUID.randomUUID();
    ShopConfiguration config = new ShopConfiguration(shopId, "UNKNOWN", Map.of());
    Order order = new Order();
    List<Order> waitingOrders = List.of();

    when(mostAvailableStrategy.supports(config)).thenReturn(false);

    // Act & Assert
    IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
        engine.assign(new QueueAssignmentContext(order, config, waitingOrders))
    );

    assertEquals("No matching strategy for shop", ex.getMessage());
  }


}
