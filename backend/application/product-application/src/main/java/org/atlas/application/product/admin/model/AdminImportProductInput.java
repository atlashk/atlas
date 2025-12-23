package org.atlas.application.product.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.file.FileType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminImportProductInput {

  private FileType fileType;

  private byte[] fileContent;
}
