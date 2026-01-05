package org.atlas.user.application.admin.mapper;

import org.atlas.user.application.admin.model.AdminRetrieveUserListInput;
import org.atlas.user.application.port.repository.criteria.FindUserCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  FindUserCriteria toFindUserCriteria(AdminRetrieveUserListInput input);
}
