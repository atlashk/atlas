package org.atlas.domain.product.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.entity.DomainEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ProductAttributeEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private Integer productId;

  @NotBlank
  private String name;

  @NotBlank
  private String value;
}
