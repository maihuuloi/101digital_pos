package com.digital.pos.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  private final RedisConnectionFactory redisConnectionFactory;

  @Bean
  public RedisCacheManager cacheManager() {
    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

    cacheConfigs.put("shop-exists", RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10)));

    cacheConfigs.put("shop-config", RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30)));

    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(5));

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigs)
        .build();
  }
}
