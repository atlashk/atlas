package org.atlas.domain.order.repository.model;

import java.math.BigDecimal;
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
  private Long orderCount;
  private BigDecimal totalAmount;
}