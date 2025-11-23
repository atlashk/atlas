package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.event.mapper.ProductEventMapper;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.admin.model.AdminCreateProductInput;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminCreateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Integer handle(AdminCreateProductInput input) throws Exception {
    Product product = input.getProduct();
    productRepository.insert(product);

    productImageService.uploadImage(product.getId(), input.getImageBytes(),
        input.getImageContentType());

    publishEvent(product);

    return product.getId();
  }

  private void publishEvent(Product product) {
    ProductEvent event = new ProductEvent(DomainEventType.PRODUCT_CREATED);
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
  }
}
