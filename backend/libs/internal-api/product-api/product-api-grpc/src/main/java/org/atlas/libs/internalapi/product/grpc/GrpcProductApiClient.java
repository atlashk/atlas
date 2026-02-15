package org.atlas.libs.internalapi.product.grpc;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.libs.protobuf.product.ListProductRequestProto;
import org.atlas.libs.protobuf.product.ListProductResponseProto;
import org.atlas.libs.protobuf.product.ProductServiceGrpc;
import org.atlas.libs.framework.internalapi.product.client.ProductApiClient;
import org.atlas.libs.framework.internalapi.product.model.RetrieveProductListInput;
import org.atlas.libs.framework.internalapi.product.model.ProductOutput;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class GrpcProductApiClient implements ProductApiClient {

  @GrpcClient("product-service")
  private ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;

  @Override
  public List<ProductOutput> call(RetrieveProductListInput request) {
    ListProductRequestProto requestProto = GrpcProductMapper.INSTANCE.map(request);
    ListProductResponseProto responseProto = productServiceBlockingStub.listProduct(requestProto);
    return GrpcProductMapper.INSTANCE.map(responseProto);
  }
}
