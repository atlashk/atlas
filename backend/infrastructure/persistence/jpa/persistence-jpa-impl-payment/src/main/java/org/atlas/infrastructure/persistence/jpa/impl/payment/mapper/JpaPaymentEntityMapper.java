package org.atlas.infrastructure.persistence.jpa.impl.payment.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.framework.payment.model.nextaction.DeepLink;
import org.atlas.framework.payment.model.nextaction.NextAction;
import org.atlas.framework.payment.model.nextaction.NextActionType;
import org.atlas.framework.payment.model.nextaction.QRCode;
import org.atlas.framework.payment.model.nextaction.RedirectUrl;
import org.atlas.framework.payment.model.nextaction.UsePaymentElement;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.persistence.jpa.impl.payment.entity.JpaPaymentEntity;

@UtilityClass
public class JpaPaymentEntityMapper {

  public static JpaPaymentEntity toJpaPaymentEntity(PaymentEntity payment) {
    JpaPaymentEntity jpaPayment = ObjectMapperUtil.getInstance()
        .map(payment, JpaPaymentEntity.class);
    jpaPayment.setNextAction(JsonUtil.getInstance().toJson(payment.getNextAction()));
    return jpaPayment;
  }

  public static PaymentEntity toPaymentEntity(JpaPaymentEntity jpaPayment) {
    PaymentEntity payment = ObjectMapperUtil.getInstance()
        .map(jpaPayment, PaymentEntity.class);

    // Parse nextAction from JSON string
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

    return payment;
  }
}
