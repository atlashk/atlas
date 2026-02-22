package org.atlas.services.identity.application.keycloak.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.user.mapper.UserInternalMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.port.in.user.service.UserInternalService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInternalServiceImpl implements UserInternalService {

  private final KeycloakUserClient keycloakUserClient;

  @Override
  public List<UserOutput> retrieveUserList(RetrieveUserListInput input) {
    List<UserEntity> userList = keycloakUserClient.retrieveUserList(input.getIds());
    return MapperUtil.mapList(userList, UserInternalMapper.INSTANCE::toUserOutput);
  }
}
