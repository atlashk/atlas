package org.atlas.services.catalog.application.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.application.product.mapper.ProductInternalMapper;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.in.product.service.ProductInternalService;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInternalServiceImpl implements ProductInternalService {

  private final ProductRepository productRepository;

  @Override
  public List<ProductOutput> retrieveProductList(RetrieveProductListInput input) {
    List<ProductEntity> productList = productRepository.findByIdIn(input.getIds());
    return MapperUtil.mapList(productList, ProductInternalMapper.INSTANCE::toProductOutput);
  }
}
