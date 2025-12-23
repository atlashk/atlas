package org.atlas.infrastructure.persistence.jpa.adapter.payment.mapper;

import org.atlas.domain.payment.entity.PaymentEvent;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.entity.JpaPaymentEvent;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaPaymentEventMapper {

  JpaPaymentEventMapper INSTANCE = Mappers.getMapper(JpaPaymentEventMapper.class);

  JpaPaymentEvent toJpaPaymentEvent(PaymentEvent paymentEvent);
}
