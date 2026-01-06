package org.atlas.payment.persistence.jpa.mapper;

import org.atlas.payment.domain.entity.PaymentEvent;
import org.atlas.payment.persistence.jpa.entity.JpaPaymentEvent;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaPaymentEventMapper {

  JpaPaymentEventMapper INSTANCE = Mappers.getMapper(JpaPaymentEventMapper.class);

  JpaPaymentEvent toJpaPaymentEvent(PaymentEvent paymentEvent);
}
