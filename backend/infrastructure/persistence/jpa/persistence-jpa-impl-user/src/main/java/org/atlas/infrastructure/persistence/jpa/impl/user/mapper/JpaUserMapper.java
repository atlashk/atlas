package org.atlas.infrastructure.persistence.jpa.impl.user.mapper;

import org.atlas.domain.user.entity.User;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaUser;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface JpaUserMapper {

  JpaUserMapper INSTANCE = Mappers.getMapper(JpaUserMapper.class);

  User toUser(JpaUser jpaUser);

  JpaUser toJpaUser(User user);
}
