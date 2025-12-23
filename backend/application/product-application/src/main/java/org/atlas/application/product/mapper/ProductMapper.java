package org.atlas.application.product.mapper;

import org.atlas.application.product.model.RetrieveProductListInput;
import org.atlas.application.product.port.fulltextsearch.SearchProductCriteria;
import org.atlas.application.product.port.repository.criteria.FindProductCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  SearchProductCriteria toSearchProductCriteria(RetrieveProductListInput input);

  FindProductCriteria toFindProductCriteria(RetrieveProductListInput input);
}
