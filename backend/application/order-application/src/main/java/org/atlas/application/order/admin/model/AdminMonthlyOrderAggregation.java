package org.atlas.application.order.admin.model;

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
public class AdminMonthlyOrderAggregation {

  private Integer year;
  private Integer month;
  private BigDecimal totalRevenue;

  public static AdminMonthlyOrderAggregation zeroFor(YearMonth ym) {
    return AdminMonthlyOrderAggregation.builder()
        .year(ym.getYear())
        .month(ym.getMonthValue())
        .totalRevenue(BigDecimal.ZERO)
        .build();
  }
}
