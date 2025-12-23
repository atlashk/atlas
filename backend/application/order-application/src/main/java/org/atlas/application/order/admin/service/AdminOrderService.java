package org.atlas.application.order.admin.service;

import java.math.BigDecimal;
import java.util.List;
import org.atlas.application.order.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.application.order.admin.model.AdminRetrieveOrderListInput;
import org.atlas.domain.order.entity.Order;
import org.atlas.framework.paging.PagingResult;

public interface AdminOrderService {

  PagingResult<Order> retrieveOrderList(AdminRetrieveOrderListInput input);

  BigDecimal retrieveTotalRevenue();

  List<AdminMonthlyOrderAggregation> retrieveMonthlyOrderStatistics();

  Long retrieveOrderCount();
}
