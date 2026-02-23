package org.atlas.services.inventory.port.in.service;

import java.util.List;
import org.atlas.libs.framework.internal.inventory.model.RetrieveStockListInput;
import org.atlas.libs.framework.internal.inventory.model.StockOutput;

public interface StockInternalService {

  List<StockOutput> retrieveStockList(RetrieveStockListInput input);
}
