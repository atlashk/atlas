package org.atlas.services.order.application.order.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.order.application.order.mapper.OrderAdminMapper;
import org.atlas.services.order.domain.entity.Order;
import org.atlas.services.order.port.in.order.model.admin.MonthlyOrderAggregation;
import org.atlas.services.order.port.in.order.model.admin.RetrieveOrderListInput;
import org.atlas.services.order.port.in.order.service.OrderAdminService;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class OrderAdminServiceImpl implements OrderAdminService {

  private final OrderRepository orderRepository;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<Order> retrieveOrderList(RetrieveOrderListInput input) {
    OrderRepository.FindOrderCriteria criteria = OrderAdminMapper.INSTANCE
        .toFindOrderCriteria(input);
    return orderRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveTotalOrderCount() {
    return orderRepository.countAll();
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
  public List<MonthlyOrderAggregation> retrieveMonthlyOrderStatistics() {
    // Fetch aggregated stats for fulfilled orders. Treat null as empty for robustness.
    List<MonthlyOrderAggregation> aggregations =
        orderRepository.aggregateMonthlyByStatus(OrderStatus.FULFILLED);
    if (CollectionUtil.isEmpty(aggregations)) {
      YearMonth now = YearMonth.now();
      return List.of(MonthlyOrderAggregation.zeroFor(now));
    }

    // Index existing aggregations by YearMonth and find the earliest month present.
    Map<YearMonth, MonthlyOrderAggregation> byMonth = new HashMap<>();
    YearMonth earliest = null;
    for (MonthlyOrderAggregation aggregation : aggregations) {
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
    List<MonthlyOrderAggregation> result = new ArrayList<>(months);

    // Walk month-by-month and fill gaps with zero-valued aggregations.
    for (YearMonth current = start; !current.isAfter(now); current = current.plusMonths(1)) {
      MonthlyOrderAggregation m = byMonth.get(current);
      result.add(m != null ? m : MonthlyOrderAggregation.zeroFor(current));
    }
    return result;
  }
}
