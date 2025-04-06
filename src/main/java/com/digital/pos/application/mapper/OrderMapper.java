package com.digital.pos.application.mapper;

import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import com.digital.pos.adapter.in.rest.model.OrderItemSummary;
import com.digital.pos.adapter.in.rest.model.OrderStatusResponse;
import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;


@Component
@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "livePosition", source = "livePosition")
  OrderCreatedResponse toOrderCreatedResponse(Order order, Integer livePosition);


  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "shopId", source = "order.shopId")
  @Mapping(target = "status", source = "order.status")
  @Mapping(target = "queueNumber", source = "order.queueNumber")
  @Mapping(target = "totalPrice", source = "order.totalPrice")
  OrderStatusResponse toOrderStatusResponse(
      Order order,
      int livePosition,
      Integer estimatedWaitMinutes
  );

  @Mapping(target = "menuItemId", source = "menuItemId")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "price", source = "price")
  @Mapping(target = "totalPrice", source = "totalPrice")
  OrderItemSummary map(OrderItem item);
}
