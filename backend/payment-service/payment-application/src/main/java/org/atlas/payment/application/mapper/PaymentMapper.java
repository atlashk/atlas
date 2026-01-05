package org.atlas.payment.application.mapper;

import org.atlas.payment.application.model.RetrievePaymentNextActionOutput;
import org.atlas.payment.domain.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  RetrievePaymentNextActionOutput toRetrievePaymentNextActionOutput(Payment payment);
}
