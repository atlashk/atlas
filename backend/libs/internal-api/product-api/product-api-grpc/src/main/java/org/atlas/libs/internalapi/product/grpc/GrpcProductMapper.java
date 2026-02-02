package org.atlas.libs.internalapi.product.grpc;

import java.util.List;
import org.atlas.libs.protobuf.product.ListProductRequestProto;
import org.atlas.libs.protobuf.product.ListProductResponseProto;
import org.atlas.libs.protobuf.product.ProductProto;
import org.atlas.libs.framework.internalapi.product.model.ListProductRequest;
import org.atlas.libs.framework.internalapi.product.model.ProductResponse;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GrpcProductMapper {

  GrpcProductMapper INSTANCE = Mappers.getMapper(GrpcProductMapper.class);

  /**
   * Maps ListProductRequest to ListProductRequestProto
   */
  @Mapping(source = "ids", target = "idList")
  ListProductRequestProto map(ListProductRequest request);

  /**
   * Maps ListProductResponseProto to List of ProductResponse
   */
  default List<ProductResponse> map(ListProductResponseProto responseProto) {
    return ObjectMapperUtil.mapList(responseProto.getProductList(), this::map);
  }

  /**
   * Maps ProductProto to ProductResponse
   */
  ProductResponse map(ProductProto productProto);
}