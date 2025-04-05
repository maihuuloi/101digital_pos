package com.digital.pos.domain.service;

import com.digital.pos.domain.model.Order;
import com.digital.pos.domain.model.ShopConfiguration;
import java.util.List;

public record QueueAssignmentContext(Order order, ShopConfiguration config, List<Order> waitingOrders) {

}
