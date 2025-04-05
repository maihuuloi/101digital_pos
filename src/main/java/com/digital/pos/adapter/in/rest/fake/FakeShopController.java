package com.digital.pos.adapter.in.rest.fake;

import com.digital.pos.adapter.out.shop.ShopConfigResponse;
import java.util.Map;
import java.util.Set;
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
public class FakeShopController {

  private static final Set<UUID> existingShopIds = Set.of(
      UUID.fromString("11111111-1111-1111-1111-111111111111"),
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
  );

  @GetMapping("/{shopId}/exists")
  public ResponseEntity<Void> shopExists(@PathVariable UUID shopId) {
    if (existingShopIds.contains(shopId)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/{shopId}/config")
  public ResponseEntity<ShopConfigResponse> getShopConfig(@PathVariable UUID shopId) {
    if (!existingShopIds.contains(shopId)) {
      return ResponseEntity.notFound().build();
    }

    ShopConfigResponse response = new ShopConfigResponse(
        shopId,
        "MOST_AVAILABLE",
        Map.of(1, 5, 2, 5, 3, 5)
    );

    return ResponseEntity.ok(response);
  }
}
