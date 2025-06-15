package org.atlas.framework.cryptography;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@UtilityClass
public class PasswordUtil {

  private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public static String hashPassword(String plainPassword) {
    return passwordEncoder.encode(plainPassword);
  }
}
