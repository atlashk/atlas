package org.atlas.common.infrastructure.api.server.rest.core.converter;

import org.atlas.common.framework.file.FileType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FileTypeConverter implements Converter<String, FileType> {

  @Override
  public FileType convert(String source) {
    return FileType.of(source);
  }
}
