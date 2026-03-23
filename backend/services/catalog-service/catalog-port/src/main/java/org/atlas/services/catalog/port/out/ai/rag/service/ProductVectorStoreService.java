package org.atlas.services.catalog.port.out.ai.rag.service;

import java.util.List;
import org.atlas.services.catalog.domain.entity.ProductEntity;

public interface ProductVectorStoreService {

  void addDocuments(List<ProductEntity> products);
}
