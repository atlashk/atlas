package org.atlas.domain.order.usecase.admin.handler;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.model.MonthlyOrderAggregation;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminGetMonthlyOrderStatisticsUseCaseHandler {

  private final OrderRepository orderRepository;

  public List<MonthlyOrderAggregation> handle() throws Exception {
    List<MonthlyOrderAggregation> aggregated =
        orderRepository.aggregateMonthlyByStatus(OrderStatus.FULFILLED);
    if (aggregated == null || aggregated.isEmpty()) {
      YearMonth now = YearMonth.now();
      MonthlyOrderAggregation zero = MonthlyOrderAggregation.builder()
          .year(now.getYear())
          .month(now.getMonthValue())
          .orderCount(0L)
          .totalAmount(BigDecimal.ZERO)
          .build();
      return List.of(zero);
    }
    List<MonthlyOrderAggregation> result = new ArrayList<>();
    MonthlyOrderAggregation first = aggregated.get(0);
    YearMonth start = YearMonth.of(first.getYear(), first.getMonth());
    YearMonth end = YearMonth.now();
    Map<String, MonthlyOrderAggregation> index = new HashMap<>();
    for (MonthlyOrderAggregation m : aggregated) {
      String key = m.getYear() + "-" + m.getMonth();
      index.put(key, m);
    }
    YearMonth current = start;
    while (!current.isAfter(end)) {
      String key = current.getYear() + "-" + current.getMonthValue();
      MonthlyOrderAggregation m = index.get(key);
      if (m == null) {
        m = MonthlyOrderAggregation.builder()
            .year(current.getYear())
            .month(current.getMonthValue())
            .orderCount(0L)
            .totalAmount(BigDecimal.ZERO)
            .build();
      }
      result.add(m);
      current = current.plusMonths(1);
    }
    return result;
  }
}