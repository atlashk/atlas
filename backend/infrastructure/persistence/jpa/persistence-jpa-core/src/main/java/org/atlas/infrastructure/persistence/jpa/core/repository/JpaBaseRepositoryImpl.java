package org.atlas.infrastructure.persistence.jpa.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class JpaBaseRepositoryImpl<T, ID extends Serializable>
    extends SimpleJpaRepository<T, ID> implements JpaBaseRepository<T, ID> {

  @PersistenceContext
  private final EntityManager entityManager;

  public JpaBaseRepositoryImpl(JpaEntityInformation<T, ID> entityInformation,
      EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
  }

  @Override
  public void insert(T entity) {
    entityManager.persist(entity);
    entityManager.flush();
  }
}
