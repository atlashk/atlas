package org.atlas.infrastructure.internalapi.payment.grpc.netdevh;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.framework.internalapi.payment.PaymentApiPort;
import org.atlas.framework.internalapi.payment.model.ListPaymentRequest;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.ListPaymentRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.ListPaymentResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.PaymentProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.PaymentServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class PaymentApiAdapter implements PaymentApiPort {

  @GrpcClient("payment-service")
  private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub;

  @Override
  public List<PaymentResponse> call(ListPaymentRequest request) {
    ListPaymentRequestProto requestProto = map(request);
    ListPaymentResponseProto responseProto = paymentServiceBlockingStub.listPayment(requestProto);
    return map(responseProto);
  }

  private ListPaymentRequestProto map(ListPaymentRequest request) {
    return ListPaymentRequestProto.newBuilder()
        .addAllId(request.getPaymentIds())
        .build();
  }

  private List<PaymentResponse> map(ListPaymentResponseProto responseProto) {
    return responseProto.getPaymentList()
        .stream()
        .map(this::map)
        .toList();
  }

  private PaymentResponse map(PaymentProto paymentProto) {
    return ObjectMapperUtil.getInstance()
        .map(paymentProto, PaymentResponse.class);
  }
}
