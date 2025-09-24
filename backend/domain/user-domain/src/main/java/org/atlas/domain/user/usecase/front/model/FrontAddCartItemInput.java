package org.atlas.domain.user.usecase.front.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrontAddCartItemInput {
  private Integer productId;
  private Integer quantity;
}
