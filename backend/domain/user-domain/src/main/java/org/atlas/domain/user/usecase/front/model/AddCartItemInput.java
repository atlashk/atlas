package org.atlas.domain.user.usecase.front.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AddCartItemInput {

  private Integer userId;
  private Integer productId;
  private Integer quantity;
}
