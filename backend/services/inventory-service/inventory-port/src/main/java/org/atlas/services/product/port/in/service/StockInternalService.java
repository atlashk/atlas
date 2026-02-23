package org.atlas.services.product.port.in.service;

import java.util.List;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;

public interface StockInternalService {

  List<StockEntity> retrieveProductList(InternalRetrieveProductListInput input);
}
