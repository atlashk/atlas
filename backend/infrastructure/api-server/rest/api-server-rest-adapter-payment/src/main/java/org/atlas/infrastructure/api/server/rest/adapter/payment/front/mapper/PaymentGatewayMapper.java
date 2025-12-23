package org.atlas.infrastructure.api.server.rest.adapter.payment.front.mapper;

import org.atlas.domain.payment.entity.PaymentGateway;
import org.atlas.infrastructure.api.server.rest.adapter.payment.front.model.PaymentGatewayResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentGatewayMapper {

  PaymentGatewayMapper INSTANCE = Mappers.getMapper(PaymentGatewayMapper.class);

  PaymentGatewayResponse toPaymentGatewayResponse(PaymentGateway paymentGateway);
}
