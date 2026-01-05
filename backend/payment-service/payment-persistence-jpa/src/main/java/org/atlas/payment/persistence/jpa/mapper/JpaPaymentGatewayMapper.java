package org.atlas.payment.persistence.jpa.mapper;

import org.atlas.payment.domain.entity.PaymentGateway;
import org.atlas.payment.persistence.jpa.entity.JpaPaymentGateway;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaPaymentGatewayMapper {

  JpaPaymentGatewayMapper INSTANCE = Mappers.getMapper(JpaPaymentGatewayMapper.class);

  PaymentGateway toPaymentGateway(JpaPaymentGateway jpaPaymentGateway);
}
