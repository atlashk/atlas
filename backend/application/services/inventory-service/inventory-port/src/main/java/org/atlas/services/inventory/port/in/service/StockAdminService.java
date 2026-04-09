package org.atlas.services.inventory.port.in.service;

import org.atlas.services.inventory.port.in.model.StockOutput;

public interface StockAdminService {

  StockOutput retrieveStock(String productId);

  void updateAvailableQuantity(String productId, Integer newAvailableQuantity);
}
