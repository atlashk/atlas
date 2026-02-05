package org.atlas.services.iam.infrastructure.persistence.jpa.mapper;

import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUser;
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

  UserEntity toUser(JpaUser jpaUser);

  JpaUser toJpaUser(UserEntity user);
}
