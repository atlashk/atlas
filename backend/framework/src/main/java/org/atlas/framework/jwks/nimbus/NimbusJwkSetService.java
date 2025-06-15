package org.atlas.framework.jwks.nimbus;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import org.atlas.framework.jwks.JwkSetService;

public class NimbusJwkSetService implements JwkSetService {

  @Override
  public Map<String, Object> generate(RSAPublicKey publicKey, String keyId) {
    RSAKey rsaKey = new RSAKey.Builder(publicKey)
        .keyID(keyId)
        .build();
    return new JWKSet(rsaKey).toJSONObject();
  }
}
