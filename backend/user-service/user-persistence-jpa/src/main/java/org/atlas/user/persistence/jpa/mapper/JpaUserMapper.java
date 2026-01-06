package org.atlas.user.persistence.jpa.mapper;

import org.atlas.user.domain.entity.User;
import org.atlas.user.persistence.jpa.entity.JpaUser;
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

  User toUser(JpaUser jpaUser);

  JpaUser toJpaUser(User user);
}
