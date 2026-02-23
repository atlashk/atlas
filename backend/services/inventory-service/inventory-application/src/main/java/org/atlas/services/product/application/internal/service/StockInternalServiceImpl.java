package org.atlas.services.product.application.internal.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.port.in.service.StockInternalService;
import org.atlas.services.product.port.out.repository.StockRepository;
import org.atlas.services.product.port.in.service.ProductImageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockInternalServiceImpl implements StockInternalService {

  private final StockRepository stockRepository;
  private final ProductImageService productImageService;

  @Override
  public List<StockEntity> retrieveProductList(InternalRetrieveProductListInput input) {
    List<StockEntity> products = stockRepository.findByIdIn(input.getIds());
    if (CollectionUtil.isEmpty(products)) {
      return Collections.emptyList();
    }

    // Update image
    products.forEach(
        product -> product.setImage(productImageService.getImage(product.getId())));

    return products;
  }
}
