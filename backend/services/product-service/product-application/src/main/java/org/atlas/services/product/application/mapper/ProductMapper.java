package org.atlas.services.product.application.mapper;

import org.atlas.services.product.application.model.RetrieveProductListInput;
import org.atlas.services.product.application.port.fulltextsearch.SearchProductCriteria;
import org.atlas.services.product.application.port.repository.criteria.FindProductCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  SearchProductCriteria toSearchProductCriteria(RetrieveProductListInput input);

  FindProductCriteria toFindProductCriteria(RetrieveProductListInput input);
}
