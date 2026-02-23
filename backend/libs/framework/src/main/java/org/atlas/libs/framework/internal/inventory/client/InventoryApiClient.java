package org.atlas.libs.framework.internal.inventory.client;

import java.util.List;
import org.atlas.libs.framework.internal.inventory.model.RetrieveStockListInput;
import org.atlas.libs.framework.internal.inventory.model.StockOutput;

public interface InventoryApiClient {

  List<StockOutput> call(RetrieveStockListInput request);
}
