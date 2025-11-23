package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.event.mapper.ProductEventMapper;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.admin.mapper.AdminProductMapper;
import org.atlas.domain.product.usecase.admin.model.AdminUpdateProductInput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.util.ArrayUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminUpdateProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Void handle(AdminUpdateProductInput input) throws Exception {
    Product product = input.getProduct();
    Product existingProduct = productRepository.findById(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    AdminProductMapper.INSTANCE.merge(product, existingProduct);
    productRepository.update(product);

    if (ArrayUtil.isNotEmpty(input.getImageBytes())) {
      productImageService.uploadImage(product.getId(), input.getImageBytes(),
          input.getImageContentType());
    }

    publishEvent(product);

    return null;
  }

  private void publishEvent(Product product) {
    ProductEvent event = new ProductEvent(DomainEventType.PRODUCT_UPDATED);
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
  }
}
