package org.atlas.domain.product.usecase.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.product.entity.Product;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateProductInput {

  private Product product;
  private byte[] imageBytes;
  private String imageContentType;
}
