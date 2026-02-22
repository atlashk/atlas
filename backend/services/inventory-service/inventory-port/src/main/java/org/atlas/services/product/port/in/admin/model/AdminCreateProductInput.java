package org.atlas.services.product.port.in.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.product.domain.entity.ProductEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminCreateProductInput {

  private ProductEntity product;
  private byte[] imageBytes;
  private String imageContentType;
}