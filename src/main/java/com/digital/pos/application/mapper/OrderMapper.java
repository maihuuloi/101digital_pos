package com.digital.pos.application.mapper;

import com.digital.pos.adapter.in.rest.model.OrderCreatedResponse;
import com.digital.pos.domain.model.Order;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;


@Component
@Mapper(componentModel = "spring")
public interface OrderMapper {

   OrderCreatedResponse toOrderCreatedResponse(Order savedOrder);
}
