package org.atlas.platform.authorization.spring.application.user.mapper;

import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.port.in.user.model.admin.CreateUserInput;
import org.atlas.platform.authorization.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.platform.authorization.port.in.user.model.admin.UpdateUserInput;
import org.atlas.platform.authorization.port.in.user.model.admin.UserOutput;
import org.atlas.platform.authorization.port.out.repository.UserRepository.FindUserCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAdminMapper {

  UserAdminMapper INSTANCE = Mappers.getMapper(UserAdminMapper.class);

  // Input --> Entity
  // -----------------------------------------------------------------------------------------------

  FindUserCriteria toFindUserCriteria(RetrieveUserListInput input);

  UserEntity toUser(CreateUserInput input);

  void merge(UpdateUserInput input, @MappingTarget UserEntity user);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  UserOutput toUser(UserEntity user);
}
