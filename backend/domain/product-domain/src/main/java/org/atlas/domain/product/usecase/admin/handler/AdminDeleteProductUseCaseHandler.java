package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.product.ProductDeletedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminDeleteProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public Void handle(Integer productId) throws Exception {
    // Delete product from DB
    ProductEntity productEntity = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(AppError.PRODUCT_NOT_FOUND));
    productRepository.delete(productEntity.getId());

    // Delete image
    productImageService.deleteImage(productEntity.getId());

    // Publish event
    publishEvent(productEntity);

    return null;
  }

  private void publishEvent(ProductEntity productEntity) {
    Product product = ObjectMapperUtil.getInstance().map(productEntity, Product.class);
    ProductDeletedEvent event = new ProductDeletedEvent(applicationConfigPort.getApplicationName(),
        product);
    externalMessagePublisherPort.publish(event);
  }
}
