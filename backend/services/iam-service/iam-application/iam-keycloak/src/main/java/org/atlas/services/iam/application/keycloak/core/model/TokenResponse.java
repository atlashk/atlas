package org.atlas.services.iam.application.keycloak.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {

  private String accessToken;

  private String refreshToken;
}
