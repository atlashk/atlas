package org.atlas.platform.authorization.spring.api.rest.user.mapper;

import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.platform.authorization.spring.api.rest.user.model.internal.RetrieveUserListRequest;
import org.atlas.platform.authorization.spring.api.rest.user.model.internal.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserInternalMapper {

  UserInternalMapper INSTANCE = Mappers.getMapper(UserInternalMapper.class);

  // Request --> Input
  // -----------------------------------------------------------------------------------------------

  RetrieveUserListInput toRetrieveUserListInput(RetrieveUserListRequest request);

  // Output --> Response
  // -----------------------------------------------------------------------------------------------

  UserResponse toUserResponse(UserOutput user);
}
