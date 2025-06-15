package org.atlas.edge.gateway.springcloudgateway.security;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.api-gateway.auth-rules")
@Data
public class AuthRulesProps {

  private List<String> nonSecuredPaths = new ArrayList<>();
  private List<SecuredPath> securedPaths = new ArrayList<>();

  @Data
  public static class SecuredPath {

    private String path;
    private List<String> roles;
  }
}
