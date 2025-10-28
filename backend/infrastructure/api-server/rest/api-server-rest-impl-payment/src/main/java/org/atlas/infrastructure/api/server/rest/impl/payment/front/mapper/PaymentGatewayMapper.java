package org.atlas.infrastructure.api.server.rest.impl.payment.front.mapper;

import org.atlas.domain.payment.entity.PaymentGateway;
import org.atlas.infrastructure.api.server.rest.impl.payment.front.model.PaymentGatewayResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentGatewayMapper {

  PaymentGatewayMapper INSTANCE = Mappers.getMapper(PaymentGatewayMapper.class);

  PaymentGatewayResponse toPaymentGatewayResponse(PaymentGateway paymentGateway);
}
