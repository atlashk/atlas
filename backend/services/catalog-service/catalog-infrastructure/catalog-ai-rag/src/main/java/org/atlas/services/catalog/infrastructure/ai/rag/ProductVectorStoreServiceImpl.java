package org.atlas.services.catalog.infrastructure.ai.rag;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.out.ai.rag.service.ProductVectorStoreService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVectorStoreServiceImpl implements ProductVectorStoreService {

  private final VectorStore vectorStore;

  @Override
  public void addDocuments(List<ProductEntity> products) {
    List<Document> documents = products.stream()
        .map(this::toDocument)
        .toList();
    vectorStore.add(documents);
  }

  private Document toDocument(ProductEntity product) {
    return Document.builder()
        .id(product.getId())
        .text(product.getDetails().getDescription())
        .metadata("name", product.getName())
        .metadata("type", product.getType().toString())
        .metadata("price", product.getPrice())
        .metadata("brand", product.getBrandName())
        .metadata("categories", product.getCategoryNames())
        .build();
  }
}
