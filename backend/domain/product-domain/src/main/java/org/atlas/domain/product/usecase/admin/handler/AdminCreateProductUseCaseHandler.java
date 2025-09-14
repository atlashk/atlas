package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.messaging.MessagePublisherPort;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminCreateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ApplicationConfigPort applicationConfigPort;
  private final MessagePublisherPort messagePublisherPort;

  public Integer handle(ProductEntity productEntity) throws Exception {
    // Insert product into DB
    productRepository.insert(productEntity);

    // Upload image
    productImageService.uploadImage(productEntity.getId(), productEntity.getImage());

    // Publish event
    publishEvent(productEntity);

    // Return inserted ID
    return productEntity.getId();
  }

  private void publishEvent(ProductEntity productEntity) {
    ProductCreatedEvent event = new ProductCreatedEvent(applicationConfigPort.getApplicationName());
    ObjectMapperUtil.getInstance()
        .merge(productEntity, event);
    event.setProductId(productEntity.getId());
    messagePublisherPort.publish(event);
  }
}
