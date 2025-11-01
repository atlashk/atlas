package org.atlas.infrastructure.persistence.jpa.impl.payment.mapper;

import org.atlas.domain.payment.entity.Payment;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.payment.model.nextaction.DeepLink;
import org.atlas.framework.payment.model.nextaction.NextAction;
import org.atlas.framework.payment.model.nextaction.NextActionType;
import org.atlas.framework.payment.model.nextaction.QRCode;
import org.atlas.framework.payment.model.nextaction.RedirectUrl;
import org.atlas.framework.payment.model.nextaction.UsePaymentElement;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.persistence.jpa.impl.payment.entity.JpaPayment;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JpaPaymentMapper {

  JpaPaymentMapper INSTANCE = Mappers.getMapper(JpaPaymentMapper.class);

  @Mapping(target = "nextAction", ignore = true)
  JpaPayment toJpaPayment(Payment payment);

  /**
   * After mapping for Payment to JpaPayment - handle NextAction serialization
   */
  @AfterMapping
  default void afterToJpaPayment(@MappingTarget JpaPayment jpaPayment, Payment payment) {
    if (payment.getNextAction() != null) {
      jpaPayment.setNextAction(JsonUtil.getInstance().toJson(payment.getNextAction()));
    }
  }

  @Mapping(target = "nextAction", ignore = true)
  Payment toPayment(JpaPayment jpaPayment);

  /**
   * After mapping for JpaPayment to Payment - handle NextAction deserialization
   */
  @AfterMapping
  default void afterToPayment(@MappingTarget Payment payment, JpaPayment jpaPayment) {
    String nextActionJson = jpaPayment.getNextAction();
    if (StringUtil.isNotBlank(nextActionJson)) {
      NextActionType nextActionType = NextActionType.valueOf(
          JsonUtil.getInstance().getAsString(nextActionJson, "type"));
      NextAction nextAction;
      switch (nextActionType) {
        case REDIRECT_URL ->
            nextAction = JsonUtil.getInstance().toObject(nextActionJson, RedirectUrl.class);
        case DEEPLINK ->
            nextAction = JsonUtil.getInstance().toObject(nextActionJson, DeepLink.class);
        case QR_CODE -> nextAction = JsonUtil.getInstance().toObject(nextActionJson, QRCode.class);
        case USE_PAYMENT_ELEMENT ->
            nextAction = JsonUtil.getInstance().toObject(nextActionJson, UsePaymentElement.class);
        default ->
            throw new IllegalStateException("Unexpected next action type: " + nextActionType);
      }
      payment.setNextAction(nextAction);
    }
  }
}
