package org.atlas.services.payment.api.server.rest.front.mapper;

import org.atlas.services.payment.api.server.rest.front.model.PaymentGatewayResponse;
import org.atlas.services.payment.domain.entity.PaymentGateway;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentGatewayMapper {

  PaymentGatewayMapper INSTANCE = Mappers.getMapper(PaymentGatewayMapper.class);

  PaymentGatewayResponse toPaymentGatewayResponse(PaymentGateway paymentGateway);
}
