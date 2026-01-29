package org.atlas.libs.fulltextsearch.elasticsearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.full-text-search.elasticsearch")
@Getter
@Setter
public class ElasticsearchProps {

  private String host;
  private String port;
  private String username;
  private String password;
}
