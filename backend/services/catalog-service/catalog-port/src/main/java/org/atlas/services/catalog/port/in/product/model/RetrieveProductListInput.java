package org.atlas.services.catalog.port.in.product.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveProductListInput {

  // Product name, description, attribute name/value, brand name, category name
  private String keyword;

  private BigDecimal minPrice;

  private BigDecimal maxPrice;

  private String brandId;

  private List<String> categoryIds;

  private PagingRequest pagingRequest;

  @Builder.Default
  private Mode mode = Mode.DATABASE;

  public enum Mode {

    DATABASE, // Database dynamic search
    SEARCH, // Full-text search
  }
}
