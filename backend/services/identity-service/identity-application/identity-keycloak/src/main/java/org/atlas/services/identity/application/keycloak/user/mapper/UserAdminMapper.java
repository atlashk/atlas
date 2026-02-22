package org.atlas.services.identity.application.keycloak.user.mapper;

import org.atlas.services.identity.application.keycloak.core.model.RetrieveUserListRequest;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAdminMapper {

  UserAdminMapper INSTANCE = Mappers.getMapper(UserAdminMapper.class);

  // Input --> Entity
  // -----------------------------------------------------------------------------------------------

  UserEntity toUser(CreateUserInput input);

  void merge(UpdateUserInput input, @MappingTarget UserEntity user);

  // Input --> Request
  // -----------------------------------------------------------------------------------------------

  RetrieveUserListRequest toRetrieveUserListRequest(RetrieveUserListInput input);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  UserOutput toUserOutput(UserEntity user);
}
