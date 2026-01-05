package org.atlas.common.framework.file;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum FileType {

  CSV("csv"),
  EXCEL("xlsx"),
  PDF("pdf");

  private final String extension;

  public static FileType of(String name) {
    return Arrays.stream(FileType.values())
        .filter(fileType -> fileType.name().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown file type: " + name));
  }
}
