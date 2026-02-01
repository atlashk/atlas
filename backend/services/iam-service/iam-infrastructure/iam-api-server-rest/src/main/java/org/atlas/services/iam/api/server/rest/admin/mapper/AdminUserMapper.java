package org.atlas.services.iam.api.server.rest.admin.mapper;

import org.atlas.services.iam.api.server.rest.admin.model.AdminUserResponse;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  @Mapping(source = "userId", target = "id")
  AdminUserResponse toUserResponse(AdminUserOutput user);
}
