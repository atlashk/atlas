package org.atlas.services.user.infrastructure.idp.enums;

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
