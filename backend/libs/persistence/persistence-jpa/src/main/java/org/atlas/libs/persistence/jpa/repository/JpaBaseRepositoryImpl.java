package org.atlas.libs.persistence.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

@Slf4j
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
    try {
      entityManager.persist(entity);
      entityManager.flush();
    } catch (DataIntegrityViolationException e) {
      // After a constraint failure, the Hibernate session still contains the corrupted entity.
      // So, we need to detach it from the session.
      entityManager.detach(entity);
      throw e;
    }
  }
}
