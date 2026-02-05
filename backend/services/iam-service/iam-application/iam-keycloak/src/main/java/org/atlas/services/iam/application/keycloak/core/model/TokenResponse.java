package org.atlas.services.iam.application.keycloak.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;
}

