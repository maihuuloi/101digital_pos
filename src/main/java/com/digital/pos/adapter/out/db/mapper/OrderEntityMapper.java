package com.digital.pos.adapter.out.db.mapper;

import com.digital.pos.adapter.out.db.entity.OrderEntity;
import com.digital.pos.adapter.out.db.entity.OrderItemEntity;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderItem;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderEntityMapper {

  public OrderEntity toJpaEntity(Order order) {
    OrderEntity entity = new OrderEntity();
    entity.setId(order.getId());
    entity.setShopId(order.getShopId());
    entity.setQueueNumber(order.getQueueNumber());
    entity.setStatus(order.getStatus());

    List<OrderItemEntity> itemEntities = order.getItems().stream()
        .map(i -> new OrderItemEntity(null, i.menuItemId(), i.quantity(), i.price(), entity))
        .toList();
    entity.setItems(itemEntities);

    return entity;
  }

  public Order toDomain(OrderEntity entity) {
    List<OrderItem> items = entity.getItems().stream()
        .map(i -> new OrderItem(i.getMenuItemId(), i.getQuantity(), i.getPrice()))
        .toList();

    return Order.builder()
        .id(entity.getId())
        .shopId(entity.getShopId())
        .queueNumber(entity.getQueueNumber())
        .status(entity.getStatus())
        .items(items)
        .build();
  }

  public List<Order> toDomainList(List<OrderEntity> entities) {
    return entities.stream()
        .map(this::toDomain)
        .toList();
  }
}
