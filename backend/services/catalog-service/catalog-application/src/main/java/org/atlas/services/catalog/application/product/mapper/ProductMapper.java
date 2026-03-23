package org.atlas.services.catalog.application.product.mapper;

import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.atlas.services.catalog.port.out.search.ProductSearchService;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductSearchService.SearchProductCriteria toSearchProductCriteria(RetrieveProductListInput input);

  ProductRepository.FindProductCriteria toFindProductCriteria(RetrieveProductListInput input);
}
