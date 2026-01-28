package org.atlas.order.application.service;

import org.atlas.order.application.model.CheckoutInput;
import org.atlas.order.application.model.RetrieveOrderListInput;
import org.atlas.order.application.model.RetrieveOrderStatusOutput;
import org.atlas.order.domain.entity.Order;
import org.atlas.common.framework.paging.PagingResult;

public interface OrderService {

  PagingResult<Order> retrieveOrderList(RetrieveOrderListInput input);

  RetrieveOrderStatusOutput retrieveOrderStatus(Integer orderId, Integer userId);

  Integer checkout(CheckoutInput input);
}
