package com.digital.pos.adapter.out.shop;

import java.util.Map;
import java.util.UUID;

public record ShopConfigResponse(UUID shopId, String queueStrategy, Map<Integer, Integer> queueCapacities) {}
