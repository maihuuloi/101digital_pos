package com.digital.pos.adapter.out.shop;

import com.digital.pos.application.port.out.ShopService;
import com.digital.pos.domain.exception.ShopConfigurationNotFoundException;
import com.digital.pos.domain.exception.ShopNotFoundException;
import com.digital.pos.domain.model.ShopConfiguration;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShopServiceImpl implements ShopService {

  private final ShopClient shopClient;

  @Override
  @Cacheable(value = "shop-exists", key = "#shopId")
  public boolean existsById(UUID shopId) {
    try {
      ResponseEntity<Void> response = shopClient.checkShopExists(shopId);
      return response.getStatusCode().is2xxSuccessful();
    } catch (FeignException.NotFound e) {
      return false;
    } catch (FeignException e) {
      throw new RuntimeException("Failed to check shop existence", e);
    }
  }

  @Override
  @Cacheable(value = "shop-config", key = "#shopId")
  public ShopConfiguration getShopConfig(UUID shopId) {
    try {
      ShopConfigResponse configResponse = shopClient.getShopConfiguration(shopId);
      return new ShopConfiguration(configResponse.shopId(), configResponse.queueStrategy(),
          configResponse.queueCapacities());
    } catch (FeignException.NotFound e) {
      throw new ShopNotFoundException(shopId);
    } catch (FeignException e) {
      throw new RuntimeException("Failed to fetch shop config for " + shopId, e);
    }
  }
}
