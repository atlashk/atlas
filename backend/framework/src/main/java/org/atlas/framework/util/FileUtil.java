package org.atlas.framework.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {

  public static File readResourceFile(String filePath) throws IOException {
    try {
      // Get the resource URL from the classpath
      URL resourceUrl = FileUtil.class.getClassLoader().getResource(filePath);
      if (resourceUrl == null) {
        throw new FileNotFoundException("Resource file not found: " + filePath);
      }

      // Convert URL to File
      return new File(resourceUrl.toURI());
    } catch (FileNotFoundException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public static InputStream readResourceFileAsStream(String filePath) throws IOException {
    InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(filePath);
    if (inputStream == null) {
      throw new IOException("Resource file not found: " + filePath);
    }
    return inputStream;
  }
}
