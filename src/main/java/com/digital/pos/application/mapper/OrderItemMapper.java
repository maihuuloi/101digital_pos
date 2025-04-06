package com.digital.pos.application.mapper;

import com.digital.pos.adapter.in.rest.model.OrderItemSummary;
import com.digital.pos.domain.model.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;


@Component
@Mapper(componentModel = "spring")
public interface OrderItemMapper {


  List<OrderItemSummary> map(List<OrderItem> items);

  @Mapping(target = "menuItemId", source = "menuItemId")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "price", source = "price")
  OrderItemSummary map(OrderItem item);
}
