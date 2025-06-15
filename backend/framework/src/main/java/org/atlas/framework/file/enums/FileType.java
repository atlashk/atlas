package org.atlas.framework.file.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum FileType {

  CSV("csv"),
  EXCEL("xlsx"),
  ;

  private final String extension;

  public static FileType of(String name) {
    for (FileType fileType : FileType.values()) {
      if (fileType.name().equalsIgnoreCase(name)) {
        return fileType;
      }
    }
    throw new IllegalArgumentException("Unknown file type: " + name);
  }

  public static FileType fromFileName(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      throw new IllegalArgumentException("File name cannot be null or empty");
    }

    // Extract file extension
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
      throw new IllegalArgumentException("File name does not have a valid extension");
    }
    String fileExtension = fileName.substring(dotIndex + 1).toLowerCase();

    for (FileType fileType : FileType.values()) {
      if (fileType.getExtension().equals(fileExtension)) {
        return fileType;
      }
    }
    throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
  }
}