package com.digital.pos.adapter.out.shop;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ShopServiceImplTest {

  @Mock
  private ShopClient shopClient;

  @InjectMocks
  private ShopServiceImpl shopService;

  @Test
  void existsById_shouldReturnTrue_whenShopExists() {
    // Arrange
    UUID shopId = UUID.randomUUID();

    when(shopClient.checkShopExists(shopId))
        .thenReturn(ResponseEntity.ok().build());

    // Act
    boolean result = shopService.existsById(shopId);

    // Assert
    assertTrue(result);
    verify(shopClient, times(1)).checkShopExists(shopId);

  }
}
