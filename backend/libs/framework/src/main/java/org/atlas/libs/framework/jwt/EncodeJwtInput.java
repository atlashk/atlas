package org.atlas.libs.framework.jwt;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EncodeJwtInput {

  private Jwt jwt;
  // RSA keys
  private RSAPublicKey rsaPublicKey;
  private RSAPrivateKey rsaPrivateKey;
}
