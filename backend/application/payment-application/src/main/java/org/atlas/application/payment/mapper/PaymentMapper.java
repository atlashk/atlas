package org.atlas.application.payment.mapper;

import org.atlas.application.payment.model.RetrievePaymentNextActionOutput;
import org.atlas.domain.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  RetrievePaymentNextActionOutput toRetrievePaymentNextActionOutput(Payment payment);
}
