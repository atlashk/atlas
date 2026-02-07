package org.atlas.services.order.application.admin.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.order.application.admin.mapper.AdminOrderMapper;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.port.in.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.services.order.port.in.admin.model.AdminOrderOutput;
import org.atlas.services.order.port.in.admin.model.AdminRetrieveOrderListInput;
import org.atlas.services.order.port.in.admin.service.AdminOrderService;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

  private final OrderRepository orderRepository;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<AdminOrderOutput> retrieveOrderList(AdminRetrieveOrderListInput input) {
    OrderRepository.FindOrderCriteria criteria = AdminOrderMapper.INSTANCE
        .toFindOrderCriteria(input);
    PagingResult<OrderEntity> orderPage = orderRepository.findByCriteria(criteria,
        input.getPagingRequest());
    return MapperUtil.mapPage(orderPage, AdminOrderMapper.INSTANCE::toAdminOrderOutput);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal retrieveTotalRevenue() {
    return orderRepository.sumAmountByStatus(OrderStatus.FULFILLED);
  }

  /**
   * Returns monthly aggregations from the earliest available month up to the current month. If the
   * repository returns no data, a single zero aggregation for the current month is returned.
   */
  @Override
  @Transactional(readOnly = true)
  public List<AdminMonthlyOrderAggregation> retrieveMonthlyOrderStatistics() {
    // Fetch aggregated stats for fulfilled orders. Treat null as empty for robustness.
    List<AdminMonthlyOrderAggregation> aggregations =
        orderRepository.aggregateMonthlyByStatus(OrderStatus.FULFILLED);
    if (CollectionUtil.isEmpty(aggregations)) {
      YearMonth now = YearMonth.now();
      return List.of(AdminMonthlyOrderAggregation.zeroFor(now));
    }

    // Index existing aggregations by YearMonth and find the earliest month present.
    Map<YearMonth, AdminMonthlyOrderAggregation> byMonth = new HashMap<>();
    YearMonth earliest = null;
    for (AdminMonthlyOrderAggregation aggregation : aggregations) {
      YearMonth ym = YearMonth.of(aggregation.getYear(), aggregation.getMonth());
      byMonth.put(ym, aggregation);
      if (earliest == null || ym.isBefore(earliest)) {
        earliest = ym;
      }
    }

    YearMonth now = YearMonth.now();
    // Guard against future-only data by starting at 'now' in that scenario.
    assert earliest != null;
    YearMonth start = !earliest.isAfter(now) ? earliest : now;

    int months = (int) ChronoUnit.MONTHS.between(start, now) + 1;
    List<AdminMonthlyOrderAggregation> result = new ArrayList<>(months);

    // Walk month-by-month and fill gaps with zero-valued aggregations.
    for (YearMonth current = start; !current.isAfter(now); current = current.plusMonths(1)) {
      AdminMonthlyOrderAggregation m = byMonth.get(current);
      result.add(m != null ? m : AdminMonthlyOrderAggregation.zeroFor(current));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveOrderCount() {
    return orderRepository.countAll();
  }
}
