package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.port.messaging.ProductMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.contract.product.ProductUpdatedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.util.StringUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminUpdateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductMessagePublisher productMessagePublisher;

  public Void handle(ProductEntity product) throws Exception {
    // Find product
    ProductEntity existingProductEntity = productRepository.findById(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Update product into DB
    ObjectMapperUtil.getInstance().merge(product, existingProductEntity);
    productRepository.update(product);

    // Upload image
    if (StringUtil.isBlank(product.getImage())) {
      productImageService.uploadImage(product.getId(), product.getImage());
    }

    // Publish event
    publishEvent(product);

    return null;
  }

  private void publishEvent(ProductEntity product) {
    Product productPayload = ObjectMapperUtil.getInstance().map(product, Product.class);
    ProductUpdatedEvent event = new ProductUpdatedEvent(productPayload);
    productMessagePublisher.publish(event);
  }
}
