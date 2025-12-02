package org.atlas.edge.auth.springsecurityjwt.core;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.service.PasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

  private final PasswordEncoder passwordEncoder;

  @Override
  public String encode(String password) {
    return passwordEncoder.encode(password);
  }
}
