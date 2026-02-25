package org.atlas.services.inventory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.security.authorization.RequiredAdmin;
import org.atlas.services.inventory.application.mapper.StockAdminMapper;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.domain.error.DomainError;
import org.atlas.services.inventory.domain.exception.DomainException;
import org.atlas.services.inventory.port.in.model.StockOutput;
import org.atlas.services.inventory.port.in.service.StockAdminService;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredAdmin
@RequiredArgsConstructor
@Slf4j
public class StockAdminServiceImpl implements StockAdminService {

  private final StockRepository stockRepository;

  @Override
  @Transactional(readOnly = true)
  public StockOutput retrieveStock(String productId) {
    StockEntity stock = getStock(productId);
    return StockAdminMapper.INSTANCE.toProductStockOutput(stock);
  }

  @Override
  @Transactional
  public void updateAvailableQuantity(String productId, Integer newAvailableQuantity) {
    StockEntity stock = getStock(productId);
    stock.setAvailableQuantity(newAvailableQuantity);
    stockRepository.update(stock);
  }

  private StockEntity getStock(String productId) {
    return stockRepository.findByProductId(productId)
        .orElseThrow(() -> new DomainException(DomainError.STOCK_NOT_FOUND));
  }
}
