package org.atlas.services.payment.api.rest.mapper;

import org.atlas.services.payment.api.rest.model.RetrievePaymentNextActionResponse;
import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  RetrievePaymentNextActionResponse toRetrievePaymentNextActionResponse(
      RetrievePaymentNextActionOutput output);
}
