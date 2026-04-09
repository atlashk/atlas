package org.atlas.libs.storage.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.minio")
@Getter
@Setter
public class MinioProps {

  private String endpoint;
  private String accessKey;
  private String secretKey;
}
