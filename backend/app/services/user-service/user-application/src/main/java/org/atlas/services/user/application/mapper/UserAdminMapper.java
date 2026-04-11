package org.atlas.services.user.application.mapper;

import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.port.in.model.admin.CreateUserInput;
import org.atlas.services.user.port.in.model.admin.RetrieveUserListInput;
import org.atlas.services.user.port.in.model.admin.UpdateUserInput;
import org.atlas.services.user.port.in.model.admin.UserOutput;
import org.atlas.services.user.port.out.repository.UserRepository.FindUserCriteria;
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

  User toUser(CreateUserInput input);

  void merge(UpdateUserInput input, @MappingTarget User user);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  UserOutput toUser(User user);
}
