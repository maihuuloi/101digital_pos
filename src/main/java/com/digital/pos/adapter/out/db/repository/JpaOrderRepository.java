package com.digital.pos.adapter.out.db.repository;

import com.digital.pos.adapter.out.db.entity.OrderEntity;
import com.digital.pos.domain.model.OrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {

  List<OrderEntity> findByShopIdAndStatus(UUID shopId, OrderStatus orderStatus);

  @Query("""
    SELECT COUNT(o2) + 1
    FROM OrderEntity o1
    JOIN OrderEntity o2
      ON o2.shopId = o1.shopId AND o2.queueNumber = o1.queueNumber
    WHERE o1.id = :orderId
      AND o2.status = 'WAITING'
      AND o2.id < o1.id
""")
  Integer findPositionInQueueOrderById(@Param("orderId") Long orderId);
}
