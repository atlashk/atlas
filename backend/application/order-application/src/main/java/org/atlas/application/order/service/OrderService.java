package org.atlas.application.order.service;

import org.atlas.application.order.model.CheckoutInput;
import org.atlas.application.order.model.RetrieveOrderListInput;
import org.atlas.application.order.model.RetrieveOrderStatusOutput;
import org.atlas.domain.order.entity.Order;
import org.atlas.framework.paging.PagingResult;

public interface OrderService {

  PagingResult<Order> retrieveOrderList(RetrieveOrderListInput input);

  RetrieveOrderStatusOutput retrieveOrderStatus(Integer orderId, Integer userId);

  Integer checkout(CheckoutInput input);
}
