package org.atlas.domain.product.usecase.admin.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.file.FileType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminImportProductInput {

  @NotNull
  private FileType fileType;

  @NotEmpty
  private byte[] fileContent;
}
