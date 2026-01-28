package org.atlas.product.application.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.product.domain.entity.Product;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminUpdateProductInput {

  private Product product;
  private byte[] imageBytes;
  private String imageContentType;
}
