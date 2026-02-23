package org.atlas.services.product.port.in.service;

import java.util.List;
import org.atlas.libs.framework.internal.inventory.model.RetrieveStockListInput;
import org.atlas.services.inventory.domain.entity.StockEntity;

public interface StockInternalService {

  List<StockEntity> retrieveStockList(RetrieveStockListInput input);
}
