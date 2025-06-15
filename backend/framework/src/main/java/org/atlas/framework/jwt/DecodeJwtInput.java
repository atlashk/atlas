package org.atlas.framework.jwt;

import java.security.interfaces.RSAPublicKey;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DecodeJwtInput {

  private String jwt;
  private String issuer;
  private RSAPublicKey rsaPublicKey;
}
