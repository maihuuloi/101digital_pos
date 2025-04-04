package com.digital.pos.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  private UUID id;
  private UUID shopId;
  private List<OrderItem> items;
  private Integer queueNumber;
  private Integer positionInQueue;
  private OrderStatus status;

  public static Order createNew(UUID shopId, List<OrderItem> items) {
    Order order = new Order();
    order.id = UUID.randomUUID();
    order.shopId = shopId;
    order.items = new ArrayList<>(items);
    order.status = OrderStatus.PENDING;
    return order;
  }

  public void assignQueue(int queueNumber, int position) {
    this.queueNumber = queueNumber;
    this.positionInQueue = position;
  }
}
