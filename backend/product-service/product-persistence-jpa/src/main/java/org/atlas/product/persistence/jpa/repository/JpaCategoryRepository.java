package org.atlas.product.persistence.jpa.repository;

import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.product.persistence.jpa.entity.JpaCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCategoryRepository extends JpaBaseRepository<JpaCategory, Integer> {

}
