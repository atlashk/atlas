package org.atlas.services.order.application.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.application.model.CheckoutInput;
import org.atlas.services.order.application.model.RetrieveOrderListInput;
import org.atlas.services.order.application.model.RetrieveOrderStatusOutput;
import org.atlas.services.order.domain.entity.Order;

public interface OrderService {

  PagingResult<Order> retrieveOrderList(RetrieveOrderListInput input);

  RetrieveOrderStatusOutput retrieveOrderStatus(Integer orderId, Integer userId);

  Integer checkout(CheckoutInput input);
}
