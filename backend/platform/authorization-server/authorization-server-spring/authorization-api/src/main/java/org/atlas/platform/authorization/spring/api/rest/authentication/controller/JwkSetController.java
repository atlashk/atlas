package org.atlas.platform.authorization.spring.api.rest.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.atlas.libs.framework.cache.Cache;
import org.atlas.libs.framework.security.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwkSetController {

  @GetMapping(value = "/.well-known/jwks.json")
  @Cache(name = "jwkSet") // Non-expiring cache
  @Operation(summary = "JwkSet endpoint")
  public Map<String, Object> jwkSet() throws Exception {
    return JwtUtil.jwkSet();
  }
}
