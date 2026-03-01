package org.atlas.services.payment.port.in.service;

import java.util.List;
import org.atlas.services.payment.domain.entity.PaymentGatewayEntity;
import org.atlas.services.payment.port.in.model.RetrievePaymentGatewayInput;
import org.atlas.services.payment.port.out.gateway.service.PaymentGatewayIntegrationService;

public interface PaymentGatewayService {

  List<PaymentGatewayEntity> retrievePaymentGatewayList();

  PaymentGatewayEntity retrievePaymentGateway(RetrievePaymentGatewayInput input);

  PaymentGatewayIntegrationService retrievePaymentGatewayIntegrationService(PaymentGatewayEntity paymentGateway);
}
