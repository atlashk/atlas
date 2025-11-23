package org.atlas.framework.domain.event.contract.product;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductEvent extends DomainEvent {

  private Integer productId;
  private String name;
  private BigDecimal price;
  private ProductStatus status;
  private ProductDetails details;
  private List<ProductAttribute> attributes;
  private Brand brand;
  private List<Category> categories;

  public ProductEvent(DomainEventType eventType) {
    super(eventType);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductDetails {

    private String description;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductAttribute {

    private Integer id;
    private String name;
    private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Brand {

    private Integer id;
    private String name;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Category {

    private Integer id;
    private String name;
  }
}
