package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.product.ProductUpdatedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.util.StringUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminUpdateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public Void handle(ProductEntity productEntity) throws Exception {
    // Find product
    ProductEntity existingProductEntity = productRepository.findById(productEntity.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Update product into DB
    ObjectMapperUtil.getInstance().merge(productEntity, existingProductEntity);
    productRepository.update(productEntity);

    // Upload image
    if (StringUtil.isBlank(productEntity.getImage())) {
      productImageService.uploadImage(productEntity.getId(), productEntity.getImage());
    }

    // Publish event
    publishEvent(productEntity);

    return null;
  }

  private void publishEvent(ProductEntity productEntity) {
    Product product = ObjectMapperUtil.getInstance().map(productEntity, Product.class);
    ProductUpdatedEvent event = new ProductUpdatedEvent(applicationConfigPort.getApplicationName(),
        product);
    externalMessagePublisherPort.publish(event);
  }
}
