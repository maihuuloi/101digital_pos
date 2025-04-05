package com.digital.pos.application.port.in;

import com.digital.pos.adapter.in.rest.model.ShopQueueResponse;
import java.util.UUID;

public interface GetQueueSnapshotUseCase {

  ShopQueueResponse getShopQueueSnapshot(UUID shopId);
}
