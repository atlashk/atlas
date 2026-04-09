package org.atlas.services.payment.application.mapper;

import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.atlas.services.payment.port.in.model.CreatePaymentInput;
import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;
import org.atlas.services.payment.port.in.model.UpdatePaymentInput;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  // Input --> Entity
  // -----------------------------------------------------------------------------------------------

  PaymentEntity toPayment(CreatePaymentInput input);

  void merge(UpdatePaymentInput input, @MappingTarget PaymentEntity payment);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  RetrievePaymentNextActionOutput toRetrievePaymentNextActionOutput(PaymentEntity payment);
}
