package org.atlas.services.order.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.persistence.DatabaseType;
import org.atlas.libs.framework.persistence.DatabaseTypeResolver;
import org.atlas.libs.persistence.jpa.helper.JpaDatabaseTypeResolver;
import org.atlas.services.order.domain.entity.CartItem;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartItemEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.mapper.JpaCartItemMapper;
import org.atlas.services.order.infrastructure.persistence.jpa.repository.JpaCartRepository;
import org.atlas.services.order.port.out.repository.CartRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCartRepositoryAdapter implements CartRepository {

  private final JpaCartRepository jpaCartRepository;
  private final DatabaseTypeResolver databaseTypeResolver;

  @Override
  public List<CartItem> findByUserId(String userId) {
    List<JpaCartItemEntity> jpaCartItems = jpaCartRepository.findByUserId(userId);
    return JpaCartItemMapper.INSTANCE.toCartItems(userId, jpaCartItems);
  }

  @Override
  public void upsertCartItem(String userId, String productId, Integer quantity) {
    DatabaseType databaseType = databaseTypeResolver.resolve();
    switch (databaseType) {
      case MYSQL -> jpaCartRepository.upsertMySql(userId, productId, quantity);
      case POSTGRES -> jpaCartRepository.upsertPostgres(userId, productId, quantity);
      default -> throw new IllegalStateException("Unsupported database type: " + databaseType);
    }
  }

  @Override
  public void updateQuantity(String userId, String productId, Integer quantity) {
    jpaCartRepository.updateQuantity(userId, productId, quantity);
  }

  @Override
  public void removeCartItem(String userId, String productId) {
    jpaCartRepository.deleteByUserIdAndProductId(userId, productId);
  }

  @Override
  public void removeAllCartItems(String userId) {
    jpaCartRepository.deleteByUserId(userId);
  }
}
