package org.atlas.infrastructure.persistence.jpa.impl.product.repository;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCategoryRepository extends JpaBaseRepository<JpaCategory, Integer> {

}
