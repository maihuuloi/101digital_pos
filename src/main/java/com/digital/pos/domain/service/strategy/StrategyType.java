package com.digital.pos.domain.service.strategy;

public enum StrategyType {
  VIP_MEMBERSHIP("VIP_MEMBERSHIP"),
  MOST_AVAILABLE("MOST_AVAILABLE");

  private final String name;

  StrategyType(String name) {
    this.name = name;
  }
}
