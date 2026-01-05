package org.atlas.user.api.server.rest.admin.mapper;

import org.atlas.user.domain.entity.User;
import org.atlas.user.api.server.rest.admin.model.AdminUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  AdminUserResponse toUserResponse(User user);
}
