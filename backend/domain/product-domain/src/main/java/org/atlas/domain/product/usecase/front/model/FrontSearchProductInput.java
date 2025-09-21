package org.atlas.domain.product.usecase.front.model;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.atlas.framework.paging.PagingRequest;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontSearchProductInput {

  private String keyword;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private Integer brandId;
  private List<Integer> categoryIds;
  @Valid
  private PagingRequest pagingRequest;
}
