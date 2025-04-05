package com.digital.pos.adapter.out.shop;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "shop-service", url = "${clients.shop-service.url}")
public interface ShopClient {


  @RequestMapping(method = RequestMethod.HEAD, value = "/api/shops/{shopId}/exists")
  ResponseEntity<Void> checkShopExists(@PathVariable("shopId") UUID shopId);

  /**
   * Fetches the configuration for a specific shop.
   *
   * @param shopId the ID of the shop
   * @return the shop configuration
   */
  @RequestMapping(method = RequestMethod.GET, value = "/api/shops/{shopId}/config")
  ShopConfigResponse getShopConfiguration(@PathVariable("shopId") UUID shopId);
}
