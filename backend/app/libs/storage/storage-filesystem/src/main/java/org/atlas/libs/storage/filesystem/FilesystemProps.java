package org.atlas.libs.storage.filesystem;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.filesystem")
@Getter
@Setter
public class FilesystemProps {

  private String root;
}
