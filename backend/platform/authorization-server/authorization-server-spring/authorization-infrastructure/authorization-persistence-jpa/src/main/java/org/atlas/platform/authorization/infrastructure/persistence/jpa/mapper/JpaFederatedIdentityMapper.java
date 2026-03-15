package org.atlas.platform.authorization.infrastructure.persistence.jpa.mapper;

import org.atlas.platform.authorization.domain.entity.FederatedIdentityEntity;
import org.atlas.platform.authorization.infrastructure.persistence.jpa.entity.JpaFederatedIdentityEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaFederatedIdentityMapper {

  JpaFederatedIdentityMapper INSTANCE = Mappers.getMapper(JpaFederatedIdentityMapper.class);

  FederatedIdentityEntity toFederatedIdentity(JpaFederatedIdentityEntity jpaFederatedIdentity);

  JpaFederatedIdentityEntity toJpaFederatedIdentity(FederatedIdentityEntity federatedIdentity);
}
