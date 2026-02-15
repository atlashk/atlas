package org.atlas.services.iam.application.keycloak.internal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.iam.application.keycloak.internal.mapper.InternalUserMapper;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.user.model.admin.UserOutput;
import org.atlas.services.iam.port.in.user.service.UserInternalService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInternalServiceImpl implements UserInternalService {

  private final KeycloakUserClient keycloakUserClient;

  @Override
  public List<UserOutput> retrieveUserList(RetrieveUserListInternalInput input) {
    List<UserEntity> userList = keycloakUserClient.retrieveUserList(input.getIds());
    return MapperUtil.mapList(userList, InternalUserMapper.INSTANCE::toInternalUserOutput);
  }
}
