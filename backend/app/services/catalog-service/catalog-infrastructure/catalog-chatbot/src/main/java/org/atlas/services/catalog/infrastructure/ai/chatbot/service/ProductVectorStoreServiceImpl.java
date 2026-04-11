package org.atlas.services.catalog.infrastructure.ai.chatbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.services.catalog.domain.entity.Brand;
import org.atlas.services.catalog.domain.entity.Category;
import org.atlas.services.catalog.domain.entity.ProductDetails;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.port.out.ai.chatbot.service.ProductVectorStoreService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVectorStoreServiceImpl implements ProductVectorStoreService {

  private final VectorStore vectorStore;

  @Override
  public void addDocuments(List<Product> products) {
    List<Document> documents = products.stream()
        .map(this::toDocument)
        .toList();
    vectorStore.add(documents);
  }

  private Document toDocument(Product product) {
    List<String> parts = new ArrayList<>();
    addPart(parts, "Product Name", product.getName());
    addPart(parts, "Brand", getBrandName(product));
    addPart(parts, "Categories", getCategoryNames(product));
    addPart(parts, "Price", String.valueOf(product.getPrice()));
    addPart(parts, "Specs", getSpecs(product));
    addPart(parts, "Description", getDescription(product));
    String content = String.join(" | ", parts);

    return Document.builder()
        .text(content)
        .metadata("productId", product.getId())
        // Add more metadata if you need to filter documents
        .build();
  }

  private void addPart(List<String> parts, String label, String value) {
    if (value != null && !value.isBlank()) {
      parts.add(label + ": " + value);
    }
  }

  private String getBrandName(Product product) {
    return Optional.ofNullable(product.getBrand())
        .map(Brand::getName)
        .orElse(null);
  }

  private String getCategoryNames(Product product) {
    return Optional.ofNullable(product.getCategories())
        .filter(list -> !list.isEmpty())
        .map(list -> list.stream()
            .map(Category::getName)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", ")))
        .orElse(null);
  }

  private String getSpecs(Product product) {
    return Optional.ofNullable(product.getAttributes())
        .filter(list -> !list.isEmpty())
        .map(list -> list.stream()
            .filter(attr -> attr.getName() != null && attr.getValue() != null)
            .map(attr -> attr.getName() + ": " + attr.getValue())
            .collect(Collectors.joining("; ")))
        .orElse(null);
  }

  private String getDescription(Product product) {
    return Optional.ofNullable(product.getDetails())
        .map(ProductDetails::getDescription)
        .orElse(null);
  }
}
