package org.atlas.infrastructure.persistence.jpa.adapter.product.repository;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCategoryRepository extends JpaBaseRepository<JpaCategory, Integer> {

}
