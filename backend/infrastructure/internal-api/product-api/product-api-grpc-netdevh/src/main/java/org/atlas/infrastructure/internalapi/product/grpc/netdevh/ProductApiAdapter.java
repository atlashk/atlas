package org.atlas.infrastructure.internalapi.product.grpc.netdevh;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.framework.internalapi.product.ProductApiPort;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ListProductRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ListProductResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ProductProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ProductServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class ProductApiAdapter implements ProductApiPort {

  @GrpcClient("product-service")
  private ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;

  @Override
  public List<ProductResponse> call(ListProductRequest request) {
    ListProductRequestProto requestProto = map(request);
    ListProductResponseProto responseProto = productServiceBlockingStub.listProduct(requestProto);
    return map(responseProto);
  }

  private ListProductRequestProto map(ListProductRequest request) {
    return ListProductRequestProto.newBuilder()
        .addAllId(request.getIds())
        .build();
  }

  private List<ProductResponse> map(ListProductResponseProto responseProto) {
    return responseProto.getProductList()
        .stream()
        .map(this::map)
        .toList();
  }

  private ProductResponse map(ProductProto productProto) {
    return ObjectMapperUtil.getInstance()
        .map(productProto, ProductResponse.class);
  }
}
