package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.port.messaging.ProductMessagePublisherPort;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.product.ProductUpdatedEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.util.StringUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminUpdateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ApplicationConfigPort applicationConfigPort;
  private final ProductMessagePublisherPort productMessagePublisherPort;

  public Void handle(ProductEntity productEntity) throws Exception {
    // Find product
    ProductEntity existingProductEntity = productRepository.findById(productEntity.getId())
        .orElseThrow(() -> new DomainException(AppError.PRODUCT_NOT_FOUND));

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
    ProductUpdatedEvent event = new ProductUpdatedEvent(applicationConfigPort.getApplicationName());
    ObjectMapperUtil.getInstance()
        .merge(productEntity, event);
    event.setProductId(productEntity.getId());
    productMessagePublisherPort.publish(event);
  }
}
