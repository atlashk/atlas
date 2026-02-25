package org.atlas.services.identity.api.rest.user.mapper;

import org.atlas.services.identity.api.rest.user.model.admin.CreateUserRequest;
import org.atlas.services.identity.api.rest.user.model.admin.RetrieveUserListRequest;
import org.atlas.services.identity.api.rest.user.model.admin.UpdateUserRequest;
import org.atlas.services.identity.api.rest.user.model.admin.UserResponse;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAdminMapper {

  UserAdminMapper INSTANCE = Mappers.getMapper(UserAdminMapper.class);

  // Request --> Input
  // -----------------------------------------------------------------------------------------------

  RetrieveUserListInput toRetrieveUserListAdminInput(RetrieveUserListRequest request);

  CreateUserInput toCreateUserInput(CreateUserRequest request);

  @Mapping(target = "id", source = "userId")
  UpdateUserInput toUpdateUserInput(UpdateUserRequest request, String userId);

  // Output --> Response
  // -----------------------------------------------------------------------------------------------

  UserResponse toUserResponse(UserOutput user);
}
