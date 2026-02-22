package org.atlas.services.product.application.front.mapper;

import org.atlas.services.product.port.in.front.model.RetrieveProductListInput;
import org.atlas.services.product.port.out.fulltextsearch.SearchProductCriteria;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  SearchProductCriteria toSearchProductCriteria(RetrieveProductListInput input);

  ProductRepository.FindProductCriteria toFindProductCriteria(RetrieveProductListInput input);
}
