package org.atlas.infrastructure.search.elasticsearch.impl.product.document;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.product.shared.ProductStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ElasticsearchProduct {

  @Id
  private String id;

  @Field(type = FieldType.Integer)
  private Integer productId;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String name;

  @Field(type = FieldType.Double)
  private BigDecimal price;

  @Field(type = FieldType.Keyword)
  private ProductStatus status;

  // Product Details
  @Field(type = FieldType.Nested)
  private ProductDetails details;

  // Attributes
  @Field(type = FieldType.Nested)
  private List<ProductAttribute> attributes;

  // Brand
  @Field(type = FieldType.Nested)
  private Brand brand;

  // Categories
  @Field(type = FieldType.Nested)
  private List<Category> categories;

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductDetails {

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductAttribute {

    @Field(type = FieldType.Integer)
    private Integer id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Brand {

    @Field(type = FieldType.Integer)
    private Integer id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Category {

    @Field(type = FieldType.Integer)
    private Integer id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
  }
}