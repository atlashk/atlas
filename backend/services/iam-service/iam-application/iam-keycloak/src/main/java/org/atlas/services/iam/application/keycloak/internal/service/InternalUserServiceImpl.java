package org.atlas.services.iam.application.keycloak.internal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.iam.application.keycloak.internal.mapper.InternalUserMapper;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.iam.port.in.internal.model.InternalUserOutput;
import org.atlas.services.iam.port.in.internal.service.InternalUserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalUserServiceImpl implements InternalUserService {

  private final KeycloakUserClient keycloakUserClient;

  @Override
  public List<InternalUserOutput> retrieveUserList(InternalRetrieveUserListInput input) {
    List<UserEntity> userList = keycloakUserClient.retrieveUserList(input.getIds());
    return MapperUtil.mapList(userList, InternalUserMapper.INSTANCE::toInternalUserOutput);
  }
}
