package org.atlas.services.product.port.in.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.product.domain.entity.Product;

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