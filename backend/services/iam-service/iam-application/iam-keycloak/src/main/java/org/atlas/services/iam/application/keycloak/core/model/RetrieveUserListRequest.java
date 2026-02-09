package org.atlas.services.iam.application.keycloak.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveUserListRequest {

  private String userId;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  private PagingRequest pagingRequest;
}
