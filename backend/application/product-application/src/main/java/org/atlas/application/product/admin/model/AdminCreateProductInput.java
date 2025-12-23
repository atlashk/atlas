package org.atlas.application.product.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.product.entity.Product;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminCreateProductInput {

  private Product product;
  private byte[] imageBytes;
  private String imageContentType;
}