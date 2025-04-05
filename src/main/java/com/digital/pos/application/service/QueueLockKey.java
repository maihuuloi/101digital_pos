package com.digital.pos.application.service;

import java.util.UUID;

public class QueueLockKey {

  public static String of(UUID shopId) {
    return "lock:shop:" + shopId;
  }
}
