package org.atlas.domain.product.usecase.internal.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

import org.atlas.framework.domain.usecase.input.InternalInput;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InternalListProductInput extends InternalInput {

  @NotEmpty
  private List<Integer> ids;
}
