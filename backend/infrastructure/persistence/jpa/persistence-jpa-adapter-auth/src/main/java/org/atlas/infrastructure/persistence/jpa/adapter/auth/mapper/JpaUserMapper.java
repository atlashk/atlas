package org.atlas.infrastructure.persistence.jpa.adapter.auth.mapper;

import org.atlas.domain.auth.entity.User;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.entity.JpaUser;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaUserMapper {

  JpaUserMapper INSTANCE = Mappers.getMapper(JpaUserMapper.class);

  User toUser(JpaUser jpaUser);

  JpaUser toJpaUser(User user);
}
