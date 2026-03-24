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
    String content = "Product Name: %s. Type: %s. Price: %s. Brand: %s. Category: %s. Description: %s"
        .formatted(
            product.getName(),
            product.getType(),
            product.getPrice(),
            product.getBrandName(),
            product.getCategoryNames(),
            product.getDetails().getDescription()
        );

    return Document.builder()
        .text(content)
        .metadata("productId", product.getId())
        .build();
  }
}
