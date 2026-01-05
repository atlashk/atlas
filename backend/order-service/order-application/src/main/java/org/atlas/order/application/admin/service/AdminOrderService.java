package org.atlas.order.application.admin.service;

import java.math.BigDecimal;
import java.util.List;
import org.atlas.order.application.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.order.application.admin.model.AdminRetrieveOrderListInput;
import org.atlas.order.domain.entity.Order;
import org.atlas.common.framework.paging.PagingResult;

public interface AdminOrderService {

  PagingResult<Order> retrieveOrderList(AdminRetrieveOrderListInput input);

  BigDecimal retrieveTotalRevenue();

  List<AdminMonthlyOrderAggregation> retrieveMonthlyOrderStatistics();

  Long retrieveOrderCount();
}
