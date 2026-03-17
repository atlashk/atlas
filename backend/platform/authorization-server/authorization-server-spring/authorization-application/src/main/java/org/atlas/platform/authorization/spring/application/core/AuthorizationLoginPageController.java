package org.atlas.platform.authorization.spring.application.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthorizationLoginPageController {

  @GetMapping("/login")
  public String loginPage() {
    return "customized-login";
  }
}
