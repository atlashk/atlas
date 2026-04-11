package org.atlas.services.user.infrastructure.persistence.jpa.mapper;

import org.atlas.services.user.domain.entity.FederatedIdentity;
import org.atlas.services.user.infrastructure.persistence.jpa.entity.JpaFederatedIdentityEntity;
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

  FederatedIdentity toFederatedIdentity(JpaFederatedIdentityEntity jpaFederatedIdentity);

  JpaFederatedIdentityEntity toJpaFederatedIdentity(FederatedIdentity federatedIdentity);
}
