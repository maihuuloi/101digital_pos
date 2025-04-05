package com.digital.pos.adapter.out.db;

import com.digital.pos.adapter.out.db.entity.OrderEntity;
import com.digital.pos.adapter.out.db.mapper.OrderEntityMapper;
import com.digital.pos.adapter.out.db.repository.JpaOrderRepository;
import com.digital.pos.application.port.out.OrderRepository;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

  private final JpaOrderRepository jpaOrderRepository;
  private final OrderEntityMapper mapper;

  @Override
  public Order save(Order order) {
    OrderEntity entity = mapper.toJpaEntity(order);
    OrderEntity saved = jpaOrderRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public List<Order> findByShopIdAndStatus(UUID shopId, OrderStatus orderStatus) {

    List<OrderEntity> orderEntities = jpaOrderRepository.findByShopIdAndStatus(shopId, orderStatus);
    return mapper.toDomainList(orderEntities);
  }

  @Override
  public Optional<Order> findById(Long orderId) {
    return jpaOrderRepository.findById(orderId)
        .map(mapper::toDomain);
  }

  @Override
  public Integer findLivePositionInQueue(Long orderId) {

    return jpaOrderRepository.findLivePositionInQueue(orderId);
  }

}
