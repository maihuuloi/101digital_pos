package com.digital.pos.adapter.in.rest;

import com.digital.pos.adapter.in.rest.api.ShopsApi;
import com.digital.pos.adapter.in.rest.model.ShopQueueResponse;
import com.digital.pos.application.port.in.GetQueueSnapshotUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Shops")
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class ShopController implements ShopsApi {

  private final GetQueueSnapshotUseCase getQueueSnapshotUseCase;

  public ResponseEntity<ShopQueueResponse> getShopQueueSnapshot(
      UUID shopId
  ) {
    log.info("Fetching queue snapshot for shop {}", shopId);
    ShopQueueResponse response = getQueueSnapshotUseCase.getShopQueueSnapshot(shopId);
    return ResponseEntity.ok(response);
  }
}
