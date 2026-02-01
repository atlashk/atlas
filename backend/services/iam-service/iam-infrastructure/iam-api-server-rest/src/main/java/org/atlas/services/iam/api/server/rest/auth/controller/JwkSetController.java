package org.atlas.services.iam.api.server.rest.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cryptography.RsaKeyLoader;
import org.atlas.libs.framework.jwks.JwkSetUtil;
import org.atlas.libs.framework.security.SecurityConstant;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known/jwks.json")
@RequiredArgsConstructor
@Slf4j
public class JwkSetController implements InitializingBean {

  private Map<String, Object> jwkSet;

  @Override
  public void afterPropertiesSet() throws Exception {
    jwkSet = JwkSetUtil.getInstance()
        .generate(RsaKeyLoader.loadPublicKey(SecurityConstant.RSA_PUBLIC_KEY_PATH),
            SecurityConstant.JWKS_KEY_ID);
  }

  @GetMapping
  @Operation(summary = "Retrieve JwkSet")
  public Map<String, Object> getJwkSet() {
    return jwkSet;
  }
}
