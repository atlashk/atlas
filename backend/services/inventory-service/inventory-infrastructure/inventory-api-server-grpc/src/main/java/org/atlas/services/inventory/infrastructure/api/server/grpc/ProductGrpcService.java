package org.atlas.services.inventory.infrastructure.api.server.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.libs.protobuf.product.ListProductRequestProto;
import org.atlas.libs.protobuf.product.ListProductResponseProto;
import org.atlas.libs.protobuf.product.ProductProto;
import org.atlas.libs.protobuf.product.ProductServiceGrpc;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.inventory.port.in.service.StockInternalService;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

  private final StockInternalService stockInternalService;

  @Override
  public void listProduct(ListProductRequestProto requestProto,
      StreamObserver<ListProductResponseProto> responseObserver) {
    InternalRetrieveProductListInput input = map(requestProto);
    try {
      List<StockEntity> products = stockInternalService.retrieveProductList(input);
      ListProductResponseProto productResponseProtoList = map(products);
      responseObserver.onNext(productResponseProtoList);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private InternalRetrieveProductListInput map(ListProductRequestProto requestProto) {
    return new InternalRetrieveProductListInput(requestProto.getIdList());
  }

  private ListProductResponseProto map(List<StockEntity> products) {
    if (CollectionUtil.isEmpty(products)) {
      return ListProductResponseProto.getDefaultInstance();
    }
    ListProductResponseProto.Builder builder = ListProductResponseProto.newBuilder();
    products.forEach(product -> builder.addProduct(map(product)));
    return builder.build();
  }

  private ProductProto map(StockEntity product) {
    return ProductProto.newBuilder()
        .setId(product.getId())
        .setName(product.getName())
        .setPrice(product.getPrice().doubleValue())
        .build();
  }
}
