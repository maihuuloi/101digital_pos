package com.digital.pos.adapter.out.menu;

import com.digital.pos.application.port.out.MenuService;
import com.digital.pos.domain.model.MenuItem;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MenuServiceImpl implements MenuService {
  private MenuClient menuClient;

  @Override
  public Set<MenuItem> getAvailableItemIds(UUID shopId) {
    List<MenuItemResponse> allItems = menuClient.getMenuItemsByShopId(shopId);

    return allItems.stream()
        .filter(MenuItemResponse::available)
        .map(dto -> new MenuItem(dto.menuItemId(), dto.name(), dto.price(), dto.available()))
        .collect(Collectors.toSet());
  }
}
