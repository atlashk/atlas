package org.atlas.infrastructure.api.server.rest.adapter.payment.front.mapper;

import org.atlas.application.payment.model.RetrievePaymentNextActionOutput;
import org.atlas.infrastructure.api.server.rest.adapter.payment.front.model.RetrievePaymentNextActionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  RetrievePaymentNextActionResponse toRetrievePaymentNextActionResponse(
      RetrievePaymentNextActionOutput output);
}
