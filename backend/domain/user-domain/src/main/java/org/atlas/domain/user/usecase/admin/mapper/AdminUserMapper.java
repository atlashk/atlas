package org.atlas.domain.user.usecase.admin.mapper;

import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.domain.user.usecase.admin.model.AdminListUserInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  FindUserCriteria toFindUserCriteria(AdminListUserInput input);
}
