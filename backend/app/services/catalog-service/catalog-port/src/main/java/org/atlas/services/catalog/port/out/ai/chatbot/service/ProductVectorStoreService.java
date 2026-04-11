package org.atlas.services.catalog.port.out.ai.chatbot.service;

import java.util.List;
import org.atlas.services.catalog.domain.entity.Product;

public interface ProductVectorStoreService {

  void addDocuments(List<Product> products);
}
