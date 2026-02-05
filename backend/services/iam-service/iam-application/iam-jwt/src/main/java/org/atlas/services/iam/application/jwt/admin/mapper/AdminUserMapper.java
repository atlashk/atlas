package org.atlas.services.iam.application.jwt.admin.mapper;

import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.out.repository.UserRepository.FindUserCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  AdminUserOutput toAdminUserOutput(UserEntity user);

  FindUserCriteria toFindUserCriteria(AdminRetrieveUserListInput input);

  UserEntity toUser(AdminCreateUserInput input);

  UserEntity toUser(AdminUpdateUserInput input);
}
