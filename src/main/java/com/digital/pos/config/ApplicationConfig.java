package com.digital.pos.config;

import com.digital.pos.domain.service.strategy.MostAvailableQueueAssignmentStrategy;
import com.digital.pos.domain.service.strategy.QueueAssignmentStrategy;
import com.digital.pos.domain.service.strategy.VipMemberShipQueueStrategy;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  List<QueueAssignmentStrategy> queueAssignmentStrategies() {
    return List.of(
        new MostAvailableQueueAssignmentStrategy(),
        new VipMemberShipQueueStrategy()
    );
  }

}
