package com.digital.pos.adapter.out.menu;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "menu-service", url = "${clients.menu-service.url}")
public interface MenuClient {

  @GetMapping("/api/shops/{shopId}/menu")
  List<MenuItemResponse> getMenuItemsByShopId(@PathVariable("shopId") UUID shopId);
}
