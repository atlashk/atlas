package org.atlas.libs.internalapi.product.grpc;

import java.util.List;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.internal.product.model.RetrieveProductListInput;
import org.atlas.libs.framework.internal.product.model.ProductOutput;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.protobuf.product.ListProductRequestProto;
import org.atlas.libs.protobuf.product.ListProductResponseProto;
import org.atlas.libs.protobuf.product.ProductProto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GrpcProductMapper {

  GrpcProductMapper INSTANCE = Mappers.getMapper(GrpcProductMapper.class);

  /**
   * Maps ListProductRequest to ListProductRequestProto
   */
  default ListProductRequestProto map(RetrieveProductListInput request) {
    if (request == null) {
      return null;
    }

    ListProductRequestProto.Builder builder = ListProductRequestProto.newBuilder();
    if (CollectionUtil.isNotEmpty(request.getIds())) {
      builder.addAllId(request.getIds());
    }
    return builder.build();
  }

  /**
   * Maps ListProductResponseProto to List of ProductResponse
   */
  default List<ProductOutput> map(ListProductResponseProto responseProto) {
    return MapperUtil.mapList(responseProto.getProductList(), this::map);
  }

  /**
   * Maps ProductProto to ProductResponse
   */
  ProductOutput map(ProductProto productProto);
}
