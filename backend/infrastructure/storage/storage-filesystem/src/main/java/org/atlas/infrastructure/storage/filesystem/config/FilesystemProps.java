package org.atlas.infrastructure.storage.filesystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.filesystem")
@Data
public class FilesystemProps {

  private String root;
}
