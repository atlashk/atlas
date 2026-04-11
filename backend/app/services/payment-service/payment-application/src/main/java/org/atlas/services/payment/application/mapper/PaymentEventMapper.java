package org.atlas.services.payment.application.mapper;

import org.atlas.services.payment.domain.entity.PaymentEvent;
import org.atlas.services.payment.port.in.model.CreatePaymentEventInput;
import org.atlas.services.payment.port.in.model.UpdatePaymentEventInput;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentEventMapper {

  PaymentEventMapper INSTANCE = Mappers.getMapper(PaymentEventMapper.class);

  // Input --> Entity
  // -----------------------------------------------------------------------------------------------

  PaymentEvent toPaymentEvent(CreatePaymentEventInput input);

  void merge(UpdatePaymentEventInput input, @MappingTarget PaymentEvent payment);
}
