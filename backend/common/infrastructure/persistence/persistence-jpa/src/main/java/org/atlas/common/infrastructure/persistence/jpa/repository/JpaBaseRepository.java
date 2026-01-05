package org.atlas.common.infrastructure.persistence.jpa.repository;

import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaBaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

  void insert(T entity);
}
