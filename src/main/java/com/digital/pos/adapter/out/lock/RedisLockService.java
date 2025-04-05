package com.digital.pos.adapter.out.lock;

import com.digital.pos.application.port.out.LockService;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class RedisLockService implements LockService {

  private final RedissonClient redissonClient;

  public RedisLockService(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  @Override
  public <T> T doWithLock(String key, Duration timeout, Duration leaseTime, Supplier<T> action) {
    RLock lock = redissonClient.getLock(key);
    boolean acquired = false;
    try {
      acquired = lock.tryLock(timeout.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
      if (!acquired) {
        throw new IllegalStateException("Unable to acquire lock for key: " + key);
      }
      return action.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Lock acquisition interrupted", e);
    } finally {
      if (acquired && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }
}
