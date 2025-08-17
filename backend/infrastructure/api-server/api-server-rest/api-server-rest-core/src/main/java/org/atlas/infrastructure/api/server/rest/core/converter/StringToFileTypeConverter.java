package org.atlas.infrastructure.api.server.rest.core.converter;

import org.atlas.framework.file.enums.FileType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFileTypeConverter implements Converter<String, FileType> {

  @Override
  public FileType convert(String source) {
    return FileType.of(source);
  }
}
