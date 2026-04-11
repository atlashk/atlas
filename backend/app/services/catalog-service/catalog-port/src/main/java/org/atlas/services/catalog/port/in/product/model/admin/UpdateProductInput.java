package org.atlas.services.catalog.port.in.product.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.Product;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UpdateProductInput {

  private Product product;

  private byte[] imageBytes;

  private String imageContentType;
}
