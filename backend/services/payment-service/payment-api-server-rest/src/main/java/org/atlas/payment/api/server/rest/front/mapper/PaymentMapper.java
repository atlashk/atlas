package org.atlas.payment.api.server.rest.front.mapper;

import org.atlas.payment.application.model.RetrievePaymentNextActionOutput;
import org.atlas.payment.api.server.rest.front.model.RetrievePaymentNextActionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  RetrievePaymentNextActionResponse toRetrievePaymentNextActionResponse(
      RetrievePaymentNextActionOutput output);
}
