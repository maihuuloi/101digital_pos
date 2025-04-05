package com.digital.pos.adapter.out.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_items_seq")
  @SequenceGenerator(name = "order_items_seq", sequenceName = "order_items_seq", allocationSize = 1)

  private Long id;

  private UUID menuItemId;

  private Integer quantity;

  private Double price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;
}
