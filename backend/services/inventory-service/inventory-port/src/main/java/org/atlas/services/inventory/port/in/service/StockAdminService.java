package org.atlas.services.inventory.port.in.service;

public interface StockAdminService {

  void updateAvailableQuantity(String productId, Integer newAvailableQuantity);
}
