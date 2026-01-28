package org.atlas.common.infrastructure.internalapi.product.grpc;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.common.framework.internalapi.product.ProductApiClient;
import org.atlas.common.framework.internalapi.product.model.ListProductRequest;
import org.atlas.common.framework.internalapi.product.model.ProductResponse;
import org.atlas.common.infrastructure.protobuf.product.ListProductRequestProto;
import org.atlas.common.infrastructure.protobuf.product.ListProductResponseProto;
import org.atlas.common.infrastructure.protobuf.product.ProductServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class GrpcProductApiClient implements ProductApiClient {

  @GrpcClient("product-service")
  private ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;

  @Override
  public List<ProductResponse> call(ListProductRequest request) {
    ListProductRequestProto requestProto = GrpcProductMapper.INSTANCE.map(request);
    ListProductResponseProto responseProto = productServiceBlockingStub.listProduct(requestProto);
    return GrpcProductMapper.INSTANCE.map(responseProto);
  }
}
