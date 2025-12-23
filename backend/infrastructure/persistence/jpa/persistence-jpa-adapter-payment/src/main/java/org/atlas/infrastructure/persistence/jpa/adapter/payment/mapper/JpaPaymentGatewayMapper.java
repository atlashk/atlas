package org.atlas.infrastructure.persistence.jpa.adapter.payment.mapper;

import org.atlas.domain.payment.entity.PaymentGateway;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.entity.JpaPaymentGateway;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaPaymentGatewayMapper {

  JpaPaymentGatewayMapper INSTANCE = Mappers.getMapper(JpaPaymentGatewayMapper.class);

  PaymentGateway toPaymentGateway(JpaPaymentGateway jpaPaymentGateway);
}
