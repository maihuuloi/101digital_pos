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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  private Long id;
  private UUID shopId;
  private List<OrderItem> items;
  private Integer queueNumber;
  private OrderStatus status;

  public static Order createNew(UUID shopId, List<OrderItem> items) {
    Order order = new Order();
    order.shopId = shopId;
    order.items = new ArrayList<>(items);
    order.status = OrderStatus.WAITING;
    return order;
  }

  public void assignQueue(int queueNumber) {
    this.queueNumber = queueNumber;
  }

  public boolean isWaiting() {

    return this.status == OrderStatus.WAITING;
  }

  public void markAsServed() {

    if (this.status == OrderStatus.WAITING) {
      this.status = OrderStatus.SERVED;
    } else {
      throw new IllegalStateException("Order is not in a state to be served");
    }
  }

  public void markAsCanceled() {

    if (this.status == OrderStatus.WAITING) {
      this.status = OrderStatus.CANCELED;
    } else {
      throw new IllegalStateException("Order is not in a state to be canceled");
    }
  }

  public Double getTotalPrice() {
    return items.stream()
        .mapToDouble(OrderItem::getTotalPrice)
        .sum();
  }
}
