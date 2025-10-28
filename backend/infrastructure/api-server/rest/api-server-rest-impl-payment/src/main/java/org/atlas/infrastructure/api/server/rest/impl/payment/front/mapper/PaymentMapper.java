package org.atlas.infrastructure.api.server.rest.impl.payment.front.mapper;

import org.atlas.domain.payment.usecase.front.model.GetPaymentNextActionOutput;
import org.atlas.infrastructure.api.server.rest.impl.payment.front.model.GetPaymentNextActionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  GetPaymentNextActionResponse toGetPaymentNextActionResponse(GetPaymentNextActionOutput output);
}
