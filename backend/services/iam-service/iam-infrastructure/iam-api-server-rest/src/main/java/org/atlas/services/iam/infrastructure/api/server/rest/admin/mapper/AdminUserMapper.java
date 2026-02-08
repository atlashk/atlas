package org.atlas.services.iam.infrastructure.api.server.rest.admin.mapper;

import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminCreateUserRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminRetrieveUserListRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminUpdateUserRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminUserResponse;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  AdminRetrieveUserListInput toAdminRetrieveUserListInput(AdminRetrieveUserListRequest request);

  AdminUserResponse toUserResponse(AdminUserOutput user);

  AdminCreateUserInput toAdminCreateUserInput(AdminCreateUserRequest request);

  AdminUpdateUserInput toAdminUpdateUserInput(AdminUpdateUserRequest request);
}
