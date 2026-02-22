package org.atlas.services.order.port.in.order.model.admin;

import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MonthlyOrderAggregation {

  private Integer year;
  private Integer month;
  private BigDecimal totalRevenue;

  public static MonthlyOrderAggregation zeroFor(YearMonth ym) {
    return MonthlyOrderAggregation.builder()
        .year(ym.getYear())
        .month(ym.getMonthValue())
        .totalRevenue(BigDecimal.ZERO)
        .build();
  }
}
