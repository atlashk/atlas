package org.atlas.common.framework.jwt;

import java.security.interfaces.RSAPublicKey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DecodeJwtInput {

  private String jwt;
  private String issuer;
  private RSAPublicKey rsaPublicKey;
}
