package org.atlas.auth.common.persistence.jpa.mapper;

import org.atlas.auth.common.domain.entity.User;
import org.atlas.auth.common.persistence.jpa.entity.JpaUser;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaUserMapper {

  JpaUserMapper INSTANCE = Mappers.getMapper(JpaUserMapper.class);

  User toUser(JpaUser jpaUser);

  JpaUser toJpaUser(User user);
}
