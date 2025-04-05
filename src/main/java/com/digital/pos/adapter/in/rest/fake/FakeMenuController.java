package com.digital.pos.adapter.in.rest.fake;

import com.digital.pos.adapter.out.menu.MenuItemResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
//@Profile("dev") // Optional: active only in dev profile
public class FakeMenuController {

  private static final Map<UUID, List<MenuItemResponse>> menuMap = new HashMap<>();

  static {
    UUID shopId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    menuMap.put(shopId, List.of(
        new MenuItemResponse(UUID.fromString("aaaa1111-aaaa-1111-aaaa-111111111111"), "Latte", 30.0, true),
        new MenuItemResponse(UUID.fromString("bbbb2222-bbbb-2222-bbbb-222222222222"), "Espresso", 25.0, true),
        new MenuItemResponse(UUID.fromString("cccc3333-cccc-3333-cccc-333333333333"), "Cappuccino", 28.0, true)
    ));
    UUID shopId2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    menuMap.put(shopId2, List.of(
        new MenuItemResponse(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), "Black Coffee", 20.0, true),
        new MenuItemResponse(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), "Mocha", 35.0, true),
        new MenuItemResponse(UUID.fromString("ffff6666-ffff-6666-ffff-666666666666"), "Americano", 22.0, true)
    ));
  }

  @GetMapping("/{shopId}/menu/available-items")
  public ResponseEntity<List<MenuItemResponse>> getAvailableItems(@PathVariable UUID shopId) {
    List<MenuItemResponse> items = menuMap.get(shopId);
    if (items == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(items);
  }
}
