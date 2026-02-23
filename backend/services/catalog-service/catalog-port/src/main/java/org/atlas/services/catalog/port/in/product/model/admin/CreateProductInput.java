package org.atlas.services.catalog.port.in.product.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.ProductEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateProductInput {

  private ProductEntity product;

  private byte[] imageBytes;

  private String imageContentType;
}
