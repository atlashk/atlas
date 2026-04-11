package org.atlas.services.payment.infrastructure.persistence.jpa.mapper;

import org.atlas.services.payment.domain.entity.PaymentEvent;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentEventEntity;
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

  PaymentEvent toPaymentEvent(JpaPaymentEventEntity jpaPaymentEvent);

  JpaPaymentEventEntity toJpaPaymentEvent(PaymentEvent paymentEvent);
}
