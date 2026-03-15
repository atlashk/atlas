package org.atlas.platform.authorization.infrastructure.persistence.jpa.mapper;

import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.infrastructure.persistence.jpa.entity.JpaUserEntity;
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

  UserEntity toUser(JpaUserEntity jpaUser);

  JpaUserEntity toJpaUser(UserEntity user);
}
