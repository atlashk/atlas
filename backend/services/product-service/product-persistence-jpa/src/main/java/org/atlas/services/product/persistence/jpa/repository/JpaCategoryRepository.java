package org.atlas.services.product.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.product.persistence.jpa.entity.JpaCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCategoryRepository extends JpaBaseRepository<JpaCategory, Integer> {

}
