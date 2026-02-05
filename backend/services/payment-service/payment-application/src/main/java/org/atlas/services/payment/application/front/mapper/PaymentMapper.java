package org.atlas.services.payment.application.front.mapper;

import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.atlas.services.payment.port.in.front.model.RetrievePaymentNextActionOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  RetrievePaymentNextActionOutput toRetrievePaymentNextActionOutput(PaymentEntity payment);
}
