package org.atlas.domain.product.usecase.admin.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.framework.file.enums.FileType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminImportProductInput {

  @NotNull
  private FileType fileType;

  @NotEmpty
  private byte[] fileContent;
}
