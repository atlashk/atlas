package org.atlas.framework.jwt;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EncodeJwtInput {

  private Jwt jwt;
  // RSA keys
  private RSAPublicKey rsaPublicKey;
  private RSAPrivateKey rsaPrivateKey;
}
