package org.atlas.services.payment.port.in.service;

import java.util.List;
import org.atlas.services.payment.domain.entity.PaymentGatewayEntity;

public interface PaymentGatewayService {

  List<PaymentGatewayEntity> retrievePaymentGatewayList();
}
