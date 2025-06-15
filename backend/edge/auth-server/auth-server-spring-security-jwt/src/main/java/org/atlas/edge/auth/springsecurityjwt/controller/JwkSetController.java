package org.atlas.edge.auth.springsecurityjwt.controller;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.constant.SecurityConstant;
import org.atlas.framework.jwks.JwkSetUtil;
import org.atlas.framework.cryptography.RsaKeyLoader;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known/jwks.json")
@RequiredArgsConstructor
@Slf4j
public class JwkSetController {

  @GetMapping
  @Cacheable(cacheNames = "jwks")
  public Map<String, Object> getJwks() throws IOException, InvalidKeySpecException {
    return JwkSetUtil.getInstance()
        .generate(RsaKeyLoader.loadPublicKey(SecurityConstant.RSA_PUBLIC_KEY_PATH),
            SecurityConstant.JWKS_KEY_ID);
  }
}
