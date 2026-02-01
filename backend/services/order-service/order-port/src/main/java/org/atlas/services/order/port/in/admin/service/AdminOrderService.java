package org.atlas.services.order.port.in.admin.service;

import java.math.BigDecimal;
import java.util.List;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.port.in.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.services.order.port.in.admin.model.AdminRetrieveOrderListInput;
import org.atlas.services.order.domain.entity.Order;

public interface AdminOrderService {

  PagingResult<Order> retrieveOrderList(AdminRetrieveOrderListInput input);

  BigDecimal retrieveTotalRevenue();

  List<AdminMonthlyOrderAggregation> retrieveMonthlyOrderStatistics();

  Long retrieveOrderCount();
}
