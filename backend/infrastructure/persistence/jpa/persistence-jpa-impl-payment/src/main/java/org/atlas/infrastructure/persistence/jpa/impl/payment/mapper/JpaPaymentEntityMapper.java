package org.atlas.infrastructure.persistence.jpa.impl.payment.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paymentgateway.model.nextaction.NextAction;
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
    payment.setNextAction(
        JsonUtil.getInstance().toObject(jpaPayment.getNextAction(), NextAction.class));
    return payment;
  }
}
