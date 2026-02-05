package org.atlas.services.iam.application.keycloak.core.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum KeycloakUserAttribute {

  PHONE_NUMBER("phoneNumber"),
  ;

  private final String name;
}
