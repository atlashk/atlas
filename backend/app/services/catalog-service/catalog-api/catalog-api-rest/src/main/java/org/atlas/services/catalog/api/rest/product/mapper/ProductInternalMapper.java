package org.atlas.services.catalog.api.rest.product.mapper;

import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.atlas.services.catalog.api.rest.product.model.internal.RetrieveProductListRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductInternalMapper {

  ProductInternalMapper INSTANCE = Mappers.getMapper(ProductInternalMapper.class);

  // Request --> Input
  // -----------------------------------------------------------------------------------------------

  RetrieveProductListInput toRetrieveProductListInput(RetrieveProductListRequest request);
}
