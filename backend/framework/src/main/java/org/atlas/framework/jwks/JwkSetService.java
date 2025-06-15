package org.atlas.framework.jwks;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

public interface JwkSetService {

  Map<String, Object> generate(RSAPublicKey publicKey, String keyId);
}
