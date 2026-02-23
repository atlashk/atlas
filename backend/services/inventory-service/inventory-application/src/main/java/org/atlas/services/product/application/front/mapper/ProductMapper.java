package org.atlas.services.product.application.front.mapper;

import org.atlas.services.product.port.in.model.RetrieveProductListInput;
import org.atlas.services.product.port.out.fulltextsearch.SearchProductCriteria;
import org.atlas.services.product.port.out.repository.StockRepository;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  SearchProductCriteria toSearchProductCriteria(RetrieveProductListInput input);

  StockRepository.FindProductCriteria toFindProductCriteria(RetrieveProductListInput input);
}
