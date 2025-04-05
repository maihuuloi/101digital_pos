package com.digital.pos.domain.service;

import com.digital.pos.domain.model.QueueAssignmentResult;

public interface QueueAssignmentEngine {

  QueueAssignmentResult assign(QueueAssignmentContext queueAssignmentContext);
}
