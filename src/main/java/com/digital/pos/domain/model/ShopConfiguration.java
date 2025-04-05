package com.digital.pos.domain.model;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public record ShopConfiguration(UUID shopId, String queueStrategy, Map<Integer, Integer> queueCapacities ) implements
    Serializable {}
