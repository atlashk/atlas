package org.atlas.product.application.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.file.FileType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminImportProductInput {

  private FileType fileType;

  private byte[] fileContent;
}
