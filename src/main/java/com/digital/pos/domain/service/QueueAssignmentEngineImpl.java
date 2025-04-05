package com.digital.pos.domain.service;

import com.digital.pos.domain.model.QueueAssignmentResult;
import com.digital.pos.domain.service.strategy.QueueAssignmentStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueAssignmentEngineImpl implements QueueAssignmentEngine {

  private final List<QueueAssignmentStrategy> strategies;

  @Override
  public QueueAssignmentResult assign(QueueAssignmentContext queueAssignmentContext) {
    return strategies.stream()
        .filter(s -> s.supports(queueAssignmentContext.config()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No matching strategy for shop"))
        .assign(queueAssignmentContext);
  }
}
