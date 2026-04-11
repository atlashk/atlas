package org.atlas.services.user.infrastructure.persistence.jpa.mapper;

import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaUserMapper {

  JpaUserMapper INSTANCE = Mappers.getMapper(JpaUserMapper.class);

  User toUser(JpaUserEntity jpaUser);

  JpaUserEntity toJpaUser(User user);
}
