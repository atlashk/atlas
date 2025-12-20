package org.atlas.domain.product.usecase.front.mapper;

import org.atlas.domain.product.infrastructure.fulltextsearch.SearchProductCriteria;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.usecase.front.model.SearchProductInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  SearchProductCriteria toSearchProductCriteria(SearchProductInput input);

  FindProductCriteria toFindProductCriteria(SearchProductInput input);
}
