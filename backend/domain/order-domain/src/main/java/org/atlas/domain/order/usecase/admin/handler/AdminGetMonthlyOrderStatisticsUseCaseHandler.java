package org.atlas.domain.order.usecase.admin.handler;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.model.MonthlyOrderAggregation;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.util.CollectionUtil;

/**
 * Use case handler that produces a contiguous time series of monthly order statistics for fulfilled
 * orders. Missing months are filled with zero-valued aggregations so that consumers can render
 * charts without gaps.
 */
@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminGetMonthlyOrderStatisticsUseCaseHandler {

  private final OrderRepository orderRepository;

  /**
   * Returns monthly aggregations from the earliest available month up to the current month. If the
   * repository returns no data, a single zero aggregation for the current month is returned.
   */
  public List<MonthlyOrderAggregation> handle() throws Exception {
    // Fetch aggregated stats for fulfilled orders. Treat null as empty for robustness.
    List<MonthlyOrderAggregation> aggregations =
        orderRepository.aggregateMonthlyByStatus(OrderStatus.FULFILLED);
    if (CollectionUtil.isEmpty(aggregations)) {
      YearMonth now = YearMonth.now();
      return List.of(zeroFor(now));
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
      result.add(m != null ? m : zeroFor(current));
    }
    return result;
  }

  /**
   * Creates a zero-valued aggregation for the given month.
   */
  private static MonthlyOrderAggregation zeroFor(YearMonth ym) {
    return MonthlyOrderAggregation.builder()
        .year(ym.getYear())
        .month(ym.getMonthValue())
        .totalRevenue(BigDecimal.ZERO)
        .build();
  }
}
