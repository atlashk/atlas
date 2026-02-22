package org.atlas.services.order.port.in.order.service;

import java.math.BigDecimal;
import java.util.List;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.port.in.order.model.admin.MonthlyOrderAggregation;
import org.atlas.services.order.port.in.order.model.admin.OrderOutput;
import org.atlas.services.order.port.in.order.model.admin.RetrieveOrderListInput;

public interface OrderAdminService {

  PagingResult<OrderOutput> retrieveOrderList(RetrieveOrderListInput input);

  Long retrieveTotalCount();

  BigDecimal retrieveTotalRevenue();

  List<MonthlyOrderAggregation> retrieveMonthlyOrderStatistics();
}
