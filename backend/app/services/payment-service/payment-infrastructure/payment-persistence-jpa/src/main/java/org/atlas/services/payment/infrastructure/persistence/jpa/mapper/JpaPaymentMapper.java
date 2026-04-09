package org.atlas.services.payment.infrastructure.persistence.jpa.mapper;

import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.atlas.services.payment.domain.entity.nextaction.DeepLink;
import org.atlas.services.payment.domain.entity.nextaction.NextAction;
import org.atlas.services.payment.domain.entity.nextaction.NextActionType;
import org.atlas.services.payment.domain.entity.nextaction.QRCode;
import org.atlas.services.payment.domain.entity.nextaction.RedirectUrl;
import org.atlas.services.payment.domain.entity.nextaction.UsePaymentElement;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaPaymentMapper {

  JpaPaymentMapper INSTANCE = Mappers.getMapper(JpaPaymentMapper.class);

  @Mapping(target = "nextAction", ignore = true)
  PaymentEntity toPayment(JpaPaymentEntity jpaPayment);

  /**
   * After mapping for JpaPayment to Payment - handle NextAction deserialization
   */
  @AfterMapping
  default void afterToPayment(@MappingTarget PaymentEntity payment, JpaPaymentEntity jpaPayment) {
    String nextActionJson = jpaPayment.getNextAction();
    if (StringUtil.isNotBlank(nextActionJson)) {
      NextActionType nextActionType = NextActionType.valueOf(
          JsonUtil.getAsString(nextActionJson, "type"));
      NextAction nextAction;
      switch (nextActionType) {
        case REDIRECT_URL ->
            nextAction = JsonUtil.toObject(nextActionJson, RedirectUrl.class);
        case DEEPLINK ->
            nextAction = JsonUtil.toObject(nextActionJson, DeepLink.class);
        case QR_CODE -> nextAction = JsonUtil.toObject(nextActionJson, QRCode.class);
        case USE_PAYMENT_ELEMENT ->
            nextAction = JsonUtil.toObject(nextActionJson, UsePaymentElement.class);
        default ->
            throw new IllegalStateException("Unexpected next action type: " + nextActionType);
      }
      payment.setNextAction(nextAction);
    }
  }

  @Mapping(target = "nextAction", ignore = true)
  JpaPaymentEntity toJpaPayment(PaymentEntity payment);

  /**
   * After mapping for Payment to JpaPayment - handle NextAction serialization
   */
  @AfterMapping
  default void afterToJpaPayment(@MappingTarget JpaPaymentEntity jpaPayment, PaymentEntity payment) {
    if (payment.getNextAction() != null) {
      jpaPayment.setNextAction(JsonUtil.toJson(payment.getNextAction()));
    }
  }
}
