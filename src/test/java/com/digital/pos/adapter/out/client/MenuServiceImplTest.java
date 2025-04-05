package com.digital.pos.adapter.out.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.digital.pos.adapter.out.menu.MenuClient;
import com.digital.pos.adapter.out.menu.MenuItemResponse;
import com.digital.pos.adapter.out.menu.MenuServiceImpl;
import com.digital.pos.domain.model.MenuItem;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

  @Mock
  MenuClient menuClient;
  @InjectMocks
  MenuServiceImpl menuService;

  @Test
  void getAvailableMenuItems_shouldReturnOnlyAvailableItems_whenMenuContainsMixedAvailability() {
    // Arrange
    UUID shopId = UUID.randomUUID();

    UUID item1 = UUID.randomUUID();
    UUID item2 = UUID.randomUUID();
    UUID item3 = UUID.randomUUID();

    List<MenuItemResponse> menuItemResponses = List.of(
        new MenuItemResponse(item1, "Espresso", 30.0, true),
        new MenuItemResponse(item2, "Cappuccino", 35.0, false),
        new MenuItemResponse(item3, "Latte", 32.0, true)
    );

    when(menuClient.getMenuItemsByShopId(shopId)).thenReturn(menuItemResponses);

    // Act
    Set<MenuItem> availableItems = menuService.getAvailableItemIds(shopId);

    // Assert
    assertEquals(2, availableItems.size());
    assertTrue(availableItems.stream().allMatch(MenuItem::available));
    assertTrue(availableItems.stream().anyMatch(item -> item.id().equals(item1)));
    assertTrue(availableItems.stream().anyMatch(item -> item.id().equals(item3)));

    // Ensure unavailable item is excluded
    assertFalse(availableItems.stream().anyMatch(item -> item.id().equals(item2)));

    // Verify Feign client was called once
    verify(menuClient, times(1)).getMenuItemsByShopId(shopId);
  }
}
