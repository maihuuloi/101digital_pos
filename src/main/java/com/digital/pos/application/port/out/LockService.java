package com.digital.pos.application.port.out;

import java.time.Duration;
import java.util.function.Supplier;

public interface LockService {
  <T> T doWithLock(String key, Duration timeout, Duration leaseTime, Supplier<T> action);
}
