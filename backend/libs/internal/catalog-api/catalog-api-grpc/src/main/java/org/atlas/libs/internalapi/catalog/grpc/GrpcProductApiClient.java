package org.atlas.libs.internalapi.catalog.grpc;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.libs.framework.internal.catalog.client.ProductApiClient;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.atlas.libs.protobuf.catalog.ProductServiceGrpc;
import org.atlas.libs.protobuf.catalog.RetrieveProductListRequestProto;
import org.atlas.libs.protobuf.catalog.RetrieveProductListResponseProto;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class GrpcProductApiClient implements ProductApiClient {

  @GrpcClient("catalog-service")
  private ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;

  @Override
  public List<ProductOutput> call(RetrieveProductListInput request) {
    RetrieveProductListRequestProto requestProto = GrpcProductMapper.INSTANCE.map(request);
    RetrieveProductListResponseProto responseProto =
        productServiceBlockingStub.retrieveProductList(requestProto);
    return GrpcProductMapper.INSTANCE.map(responseProto);
  }
}
