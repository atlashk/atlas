package org.atlas.services.payment.infrastructure.persistence.jpa.mapper;

import org.atlas.services.payment.domain.entity.PaymentGateway;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentGateway;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaPaymentGatewayMapper {

  JpaPaymentGatewayMapper INSTANCE = Mappers.getMapper(JpaPaymentGatewayMapper.class);

  PaymentGateway toPaymentGateway(JpaPaymentGateway jpaPaymentGateway);
}
