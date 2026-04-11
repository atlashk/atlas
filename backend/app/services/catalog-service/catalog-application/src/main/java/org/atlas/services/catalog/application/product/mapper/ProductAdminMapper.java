package org.atlas.services.catalog.application.product.mapper;

import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.port.in.product.model.admin.ExportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.RetrieveProductListInput;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductAdminMapper {

  ProductAdminMapper INSTANCE = Mappers.getMapper(ProductAdminMapper.class);

  ProductRepository.FindProductCriteria toFindProductCriteria(RetrieveProductListInput input);

  ProductRepository.FindProductCriteria toFindProductCriteria(ExportProductInput input);

  void merge(Product source, @MappingTarget Product target);
}
