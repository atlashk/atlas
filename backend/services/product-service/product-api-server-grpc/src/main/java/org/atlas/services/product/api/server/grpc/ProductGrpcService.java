package org.atlas.services.product.api.server.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.libs.protobuf.product.ListProductRequestProto;
import org.atlas.libs.protobuf.product.ListProductResponseProto;
import org.atlas.libs.protobuf.product.ProductProto;
import org.atlas.libs.protobuf.product.ProductServiceGrpc;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.product.application.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.application.internal.service.InternalProductService;
import org.atlas.services.product.domain.entity.Product;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

  private final InternalProductService internalProductService;

  @Override
  public void listProduct(ListProductRequestProto requestProto,
      StreamObserver<ListProductResponseProto> responseObserver) {
    InternalRetrieveProductListInput input = map(requestProto);
    try {
      List<Product> products = internalProductService.retrieveProductList(input);
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

  private ListProductResponseProto map(List<Product> products) {
    if (CollectionUtil.isEmpty(products)) {
      return ListProductResponseProto.getDefaultInstance();
    }
    ListProductResponseProto.Builder builder = ListProductResponseProto.newBuilder();
    products.forEach(product -> builder.addProduct(map(product)));
    return builder.build();
  }

  private ProductProto map(Product product) {
    return ProductProto.newBuilder()
        .setId(product.getId())
        .setName(product.getName())
        .setPrice(product.getPrice().doubleValue())
        .build();
  }
}
