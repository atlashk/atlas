package org.atlas.libs.framework.jwt;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.UserRole;

@Getter
@Setter
@Builder
public class Jwt {

  // 'jti' claim
  private String jwtId;
  // 'iss' claim
  private String issuer;
  // 'iat' claim
  private Date issuedAt;
  // 'sub' claim (userId)
  private String subject;
  // 'aud' claim
  private String audience;
  // 'exp' claim
  private Date expiresAt;
  // Custom claims
  private UserRole userRole;

  public String getUserId() {
    return subject;
  }
}
