package org.atlas.services.order.port.in.front.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.port.in.front.model.CheckoutInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderListInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderStatusOutput;

public interface OrderService {

  PagingResult<OrderEntity> retrieveOrderList(RetrieveOrderListInput input);

  RetrieveOrderStatusOutput retrieveOrderStatus(String orderId, String userId);

  String checkout(CheckoutInput input);
}
