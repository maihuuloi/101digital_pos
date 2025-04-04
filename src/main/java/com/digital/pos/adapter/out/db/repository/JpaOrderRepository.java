package com.digital.pos.adapter.out.db.repository;

import com.digital.pos.adapter.out.db.entity.OrderEntity;
import com.digital.pos.domain.model.OrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {

  List<OrderEntity> findByShopIdAndStatus(UUID shopId, OrderStatus orderStatus);
}
