package org.atlas.services.order.port.in.order.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.port.in.order.model.CheckoutInput;
import org.atlas.services.order.port.in.order.model.RetrieveOrderListInput;
import org.atlas.services.order.port.in.order.model.RetrieveOrderStatusOutput;

public interface OrderService {

  PagingResult<OrderEntity> retrieveOrderList(RetrieveOrderListInput input);

  RetrieveOrderStatusOutput retrieveOrderStatus(String id);

  String checkout(CheckoutInput input);
}
