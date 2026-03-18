package org.atlas.services.inventory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.services.inventory.application.mapper.StockAdminMapper;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.domain.error.InventoryDomainError;
import org.atlas.services.inventory.port.in.model.StockOutput;
import org.atlas.services.inventory.port.in.service.StockAdminService;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
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
        .orElseThrow(() -> new DomainException(InventoryDomainError.STOCK_NOT_FOUND));
  }
}
