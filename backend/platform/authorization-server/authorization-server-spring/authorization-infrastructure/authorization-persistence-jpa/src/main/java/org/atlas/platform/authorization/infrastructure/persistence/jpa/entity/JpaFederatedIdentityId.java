package org.atlas.platform.authorization.infrastructure.persistence.jpa.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.security.FederatedIdentityProvider;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JpaFederatedIdentityId implements Serializable {

  private String userId;

  private FederatedIdentityProvider provider;
}
