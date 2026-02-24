package org.atlas.libs.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class IssueTokenInput {

  private String userId;
  private UserRole role;
}
